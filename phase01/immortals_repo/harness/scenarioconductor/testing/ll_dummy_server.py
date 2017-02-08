#!/usr/bin/env python
import SocketServer
import json
import logging

import bottle

from . import ta_submitter as tas

_failure_map = {
    'baseline': 'scenarioconductor/configs/samples/scenarioconfiguration-baseline.json',
    'fail-all': 'scenarioconductor/configs/samples/scenarioconfiguration-baseline-b-fail.json',
    'fail-gps': 'scenarioconductor/configs/samples/scenarioconfiguration-baseline-b-fail-gps.json',
    'fail-bandwidth': 'scenarioconductor/configs/samples/scenarioconfiguration-baseline-b-fail-bandwidth.json'
}


# noinspection PyClassHasNoInit
class ExecutionPath:
    BaselineValidation = tas.send_baseline_validation,
    AdaptationAndValidation = tas.send_adapt_and_validate


class LLHarness(bottle.Bottle):
    def __init__(self, host, port, execution_scenarios, log_filepath):
        super(LLHarness, self).__init__()

        lfh = logging.FileHandler(log_filepath)
        lfh.setFormatter(logging.Formatter('%(message)s'))
        self.network_logger = logging.getLogger(log_filepath)
        self.network_logger.setLevel(logging.INFO)
        self.network_logger.addHandler(lfh)

        tas.set_logger(self.network_logger)

        self.pending_executions = execution_scenarios
        self.submitted_executions = []
        self._define_routes()

        SocketServer.TCPServer.allow_reuse_address = True
        SocketServer.ThreadingTCPServer.allow_reuse_address = True

        self.server = bottle.CherryPyServer(host=host, port=port)

    def start(self):
        bottle.run(app=self, server=self.server, debug=True)

    def _define_routes(self):
        self.route(path='/error', method='POST', callback=self._error)
        self.route(path='/ready', method='POST', callback=self._ready)
        self.route(path='/status', method='POST', callback=self._status)
        self.route(path='/action/done', method='POST', callback=self._action_done)

    def _error(self):
        obj_j = bottle.request.json
        self.network_logger.info(
            'TH RECEIVED POST /error with BODY: ' + json.dumps(obj_j, indent=4, separators=(',', ': ')) + '\n')

    def _ready(self):
        obj_j = bottle.request.json
        self.network_logger.info(
            'TH RECEIVED POST /ready with BODY: ' + json.dumps(obj_j, indent=4, separators=(',', ': ')) + '\n')

        self.pending_executions[0].execute()

    def _status(self):
        obj_j = bottle.request.json
        self.network_logger.info(
            'TH RECEIVED POST /status with BODY: ' + json.dumps(obj_j, indent=4, separators=(',', ': ')) + '\n')

    def _action_done(self):
        obj_j = bottle.request.json
        self.network_logger.info('TH RECEIVED POST /action/done with BODY: ' + json.dumps(obj_j, indent=4,
                                                                                          separators=(
                                                                                              ',', ': ')) + '\n')
        self.pending_executions.pop(0)

        if len(self.pending_executions) > 0:
            self.pending_executions[0].execute()


def main(args):
    if args.test_harness_port is not None:
        tas.TH_PORT = args.test_harness_port

    if args.test_harness_address is not None:
        tas.TH_URL = args.test_harness_address

    if args.test_adapter_port is not None:
        tas.TA_PORT = args.test_adapter_port

    if args.test_adapter_address is not None:
        tas.TA_URL = args.test_adapter_address

    tas.URL_TEMPLATE = tas.TA_PROTOCOL + tas.TA_URL + ':' + str(tas.TA_PORT) + '/{path}'

    scc_j = None

    if args.deployment_model == 'custom':

        if args.scenario_file is not None:
            scc_j = json.load(open(args.scenario_file, 'r'))

        elif args.scenario_string is not None:
            scc_j = json.loads(args.scenario_string)

        else:
            raise Exception("Cannot execute a 'custom' deployment model without a provided file or string!")

    elif args.deployment_model != 'custom':
        if args.scenario_file is None and args.scenario_string is None:
            scc_j = json.load(open(_failure_map[args.deployment_model]))
        else:
            raise Exception(
                "Cannot execute a deployment model file or string unless the deployment model type is set to 'custom'!")

    execution_scenarios = tas.produce_test_adapter_submissions(args.flow, scc_j)

    o = LLHarness(host=tas.TH_URL, port=tas.TH_PORT, execution_scenarios=execution_scenarios,
                  log_filepath='ll_dummy_server.log')

    o.start()
