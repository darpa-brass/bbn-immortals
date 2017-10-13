#!/usr/bin/env python
import json
import os
import shutil
import socketserver
import time

import bottle

from pymmortals.datatypes.deployment_model import LLP1Input
from pymmortals import threadprocessrouter as tpr
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.testing_p1 import Endpoint, SubmissionFlow, AbstractHarnessListener
from pymmortals.resources import resourcemanager
from pymmortals.testing.phase1.harness_listeners import DefaultExecutionListener
from pymmortals.utils import clean_json_str
from . import ta_submitter as tas


class TestHarnesstEndpoint:
    def ready(self, val: str):
        raise NotImplementedError

    def error(self, val: str):
        raise NotImplementedError

    def status(self, val: str):
        raise NotImplementedError

    def done(self, val: str):
        raise NotImplementedError


# noinspection PyClassHasNoInit
class ExecutionPath:
    BaselineValidation = tas.send_baseline_validation,
    AdaptationAndValidation = tas.send_adapt_and_validate


class LLHarness(bottle.Bottle):
    def __init__(self, host: str, port: int, logger: AbstractHarnessListener):
        super(LLHarness, self).__init__()

        self._logger = logger
        tas.set_logger(self._logger)

        self._define_routes()

        self._results = []

        socketserver.TCPServer.allow_reuse_address = True
        socketserver.ThreadingTCPServer.allow_reuse_address = True

        # self.server = bottle.TornadoServer(host=host, port=port)
        self.server = bottle.CherryPyServer(host=host, port=port)

    def start(self):

        target_directories = [
            get_configuration().dataRoot,
            get_configuration().artifactRoot
        ]

        for d in target_directories:
            if not os.path.exists(d):
                try:
                    os.mkdir(d)
                except:
                    raise Exception('Could not create directory "' + d + '". Are you sure the'
                                    + ' parent directory exists and is writeable?')

        bottle.run(app=self, server=self.server, debug=True)

    def _define_routes(self):
        self.route(path=Endpoint.ERROR.path, method='POST', callback=self._error)
        self.route(path=Endpoint.READY.path, method='POST', callback=self._ready)
        self.route(path=Endpoint.STATUS.path, method='POST', callback=self._status)
        self.route(path=Endpoint.ACTION_DONE.path, method='POST', callback=self._action_done)

    def _error(self):
        body_str = bottle.request.body.read()

        if isinstance(body_str, bytes):
            body_str = body_str.decode()

        print('##' + body_str)
        self._logger.receiving_post_listener(Endpoint.ERROR, body_str)

    def _ready(self):
        body_str = bottle.request.body.read()

        if isinstance(body_str, bytes):
            body_str = body_str.decode()

        print('##' + body_str)
        self._logger.receiving_post_listener(Endpoint.READY, body_str)

    def _status(self):
        body_str = bottle.request.body.read()

        if isinstance(body_str, bytes):
            body_str = body_str.decode()

        print('##' + body_str)
        self._logger.receiving_post_listener(Endpoint.STATUS, body_str)

    def _action_done(self):
        body_str = bottle.request.body.read()

        if isinstance(body_str, bytes):
            body_str = body_str.decode()

        print('##' + body_str)

        self._logger.receiving_post_listener(Endpoint.ACTION_DONE, body_str)


def main(args):
    scc_j = None

    if args.deployment_model == 'custom':

        if args.scenario_file is not None:
            scc_j = json.load(open(args.scenario_file, 'r'))

        elif args.scenario_string is not None:
            scc_j = json.loads(args.scenario_string)

        else:
            timestamp = int(time.time() * 1000)
            custom_json_file = str(timestamp) + '-deployment_model.json'
            shutil.copy('sample_submission_phase01.json', custom_json_file)
            tpr.global_subprocess.run(['vi', custom_json_file])

            orig_lines = open('sample_submission_phase01.json', 'r').readlines()
            new_lines = open(custom_json_file, 'r').readlines()

            has_changed = True

            if len(orig_lines) == len(new_lines):
                has_changed = False

                for idx in range(len(orig_lines)):
                    if orig_lines[idx] != new_lines[idx]:
                        has_changed = True
                        break

            if not has_changed:
                raise Exception("The submission file has not been modified! Aborting!")
            else:
                scc_j = json.loads(clean_json_str(open(custom_json_file, 'r').read()))

    elif args.deployment_model != 'custom':
        if args.scenario_file is None and args.scenario_string is None:
            scc_j = resourcemanager.get_phase1_input_dict(args.deployment_model)
        else:
            raise Exception(
                "Cannot execute a deployment model file or string unless the deployment model type is set to 'custom'!")

    ll_input = LLP1Input.from_dict(scc_j)

    execution_listener = DefaultExecutionListener(log_filepath='ll_dummy_server.log',
                                                  submission_flow=SubmissionFlow(args.flow),
                                                  ll_input=ll_input)
    thc = get_configuration().testHarness

    o = LLHarness(host=thc.url, port=thc.port, logger=execution_listener)

    o.start()
