#!/usr/bin/env python
import SocketServer
import argparse
import json
import logging

import bottle

from . import ta_submitter as tas

parser = argparse.ArgumentParser(description='IMMORTALS Mock LL TH')

_failure_map = {
    'none': 'scenarioconductor/configs/samples/scenarioconfiguration-baseline.json',
    'all': 'scenarioconductor/configs/samples/scenarioconfiguration-baseline-b-fail.json',
    'gps': 'scenarioconductor/configs/samples/scenarioconfiguration-baseline-b-fail-gps.json',
    'bandwidth': 'scenarioconductor/configs/samples/scenarioconfiguration-baseline-b-fail-bandwidth.json'
}


# _DEFAULT_PERTURBED_SUBMISSION_FILE = 'scenarioconductor/configs/samples/scenarioconfiguration-challenge-fail.json'


def add_parser_arguments(psr):
    psr.add_argument('-thp', '--test-harness-port', type=int)
    psr.add_argument('-tha', '--test-harness-address', type=str)
    psr.add_argument('-tap', '--test-adapter-port', type=int)
    psr.add_argument('-taa', '--test-adapter-address', type=str)
    psr.add_argument('flow', metavar='FLOW', choices=['baselineA', 'baselineB', 'challenge', 'all'])
    psr.add_argument('failure', metavar='FAILURE_PROPERTY', choices=['none', 'all', 'gps', 'bandwidth'])
    psr.add_argument('-f', '--scenario-file', type=str)
    psr.add_argument('-s', '--scenario-string', type=str)


add_parser_arguments(parser)


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


def main(args=None):
    if args is None:
        args = parser.parse_args()

    if args.test_harness_port is not None:
        tas.TH_PORT = args.test_harness_port

    if args.test_harness_address is not None:
        tas.TH_URL = args.test_harness_address

    if args.test_adapter_port is not None:
        tas.TA_PORT = args.test_adapter_port

    if args.test_adapter_address is not None:
        tas.TA_URL = args.test_adapter_address

    tas.URL_TEMPLATE = tas.TA_PROTOCOL + tas.TA_URL + ':' + str(tas.TA_PORT) + '/{path}'

    if args.scenario_file is not None:
        scc_j = json.load(open(args.scenario_file, 'r'))

    elif args.scenario_string is not None:
        scc_j = json.loads(args.scenario_string)

    else:
        scc_j = json.load(open(_failure_map[args.failure]))

    execution_scenarios = tas.produce_test_adapter_submissions(args.flow, scc_j)

    o = LLHarness(host=tas.TH_URL, port=tas.TH_PORT, execution_scenarios=execution_scenarios,
                  log_filepath='ll_dummy_server.log')

    o.start()


if __name__ == '__main__':
    main()
