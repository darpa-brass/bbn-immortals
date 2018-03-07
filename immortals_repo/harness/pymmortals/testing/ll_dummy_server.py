#!/usr/bin/env python
import socketserver
import time
from typing import Dict, Union, Callable

import bottle
from cherrypy import wsgiserver

from pymmortals import immortalsglobals as ig
from pymmortals.datatypes.testing import Phase2TestHarnessListenerInterface, Phase2TestScenario
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.testharnessendpoint import TestHarnessEndpoint
from pymmortals.immortalsglobals import get_configuration
from pymmortals.testing.harness_listeners import TABehaviorValidator
from . import ta_submitter as tas


class ImmortalsLLServer(bottle.ServerAdapter):
    def __init__(self, host='127.0.0.1', port=8080, **config):
        super().__init__(host, port, **config)
        self.server = None  # type: wsgiserver.CherryPyWSGIServer

    def run(self, handler):  # pragma: no cover
        self.server = wsgiserver.CherryPyWSGIServer((self.host, self.port), handler)
        try:
            self.server.start()
        finally:
            self.server.stop()

    def stop(self):
        self.server.stop()


class LLHarness(bottle.Bottle):
    def __init__(self, host: str, port: int,
                 done_listener: Callable = None,
                 done_listener_is_self: bool = False):
        super(LLHarness, self).__init__()

        self._define_routes()

        self._results = []

        socketserver.TCPServer.allow_reuse_address = True
        socketserver.ThreadingTCPServer.allow_reuse_address = True

        self.server = ImmortalsLLServer(host=host, port=port)

        self._done_listener = self.stop if done_listener_is_self else done_listener
        self._event_listener = None  # type: Phase2TestHarnessListenerInterface

    def load_test(self, test_suite_identifier: str, test_identifier: str) -> Phase2TestScenario:
        """
        :return: The first test scenario to run
        """
        self._event_listener = TABehaviorValidator(done_listener=self._done_listener,
                                                   test_suite_identifier=test_suite_identifier,
                                                   test_identifier=test_identifier)
        tas.set_logger(self._event_listener)

        return self._event_listener.get_current_test_scenario()

    def start(self):
        bottle.run(app=self, server=self.server, debug=True)

    def stop(self, test_scenario: Phase2TestScenario = None):
        if self.server is not None:
            self.server.stop()

    def _define_routes(self):
        # TODO: The time.sleep's in the methods should not be necessary. But jersey refuses connections otherwise...
        self.route(path=TestHarnessEndpoint.READY.path,
                   method=TestHarnessEndpoint.READY.restType.name, callback=self._ready)
        self.route(path=TestHarnessEndpoint.ERROR.path,
                   method=TestHarnessEndpoint.ERROR.restType.name, callback=self._error)
        self.route(path=TestHarnessEndpoint.STATUS.path,
                   method=TestHarnessEndpoint.STATUS.restType.name, callback=self._status)
        self.route(path=TestHarnessEndpoint.DONE.path,
                   method=TestHarnessEndpoint.DONE.restType.name, callback=self._done)

    def _error(self):
        body_str = bottle.request.body.read().decode()

        time.sleep(.2)
        self._event_listener.receiving_post_listener(TestHarnessEndpoint.ERROR, body_str)
        self._set_success_response(endpoint=TestHarnessEndpoint.ERROR)

    def _ready(self):
        body_str = bottle.request.body.read().decode()

        time.sleep(.2)
        self._event_listener.receiving_post_listener(TestHarnessEndpoint.READY, body_str)
        self._set_success_response(endpoint=TestHarnessEndpoint.READY)

    def _status(self):
        body_str = bottle.request.body.read().decode()

        time.sleep(.2)
        self._event_listener.receiving_post_listener(TestHarnessEndpoint.STATUS, body_str)
        self._set_success_response(endpoint=TestHarnessEndpoint.STATUS)

    def _done(self):
        body_str = bottle.request.body.read().decode()

        time.sleep(.2)
        self._event_listener.receiving_post_listener(TestHarnessEndpoint.DONE, body_str)
        self._set_success_response(endpoint=TestHarnessEndpoint.DONE)

    def _set_success_response(self, endpoint: TestHarnessEndpoint, body: Union[str, Dict, None] = None):
        if body is None:
            pass

        elif isinstance(body, Dict):
            bottle.response.set_header('Content-Type', 'application/json')

        elif isinstance(body, str):
            bottle.response.set_header('Content-Type', 'text/plain')

        else:
            raise Exception('Unexpected body type "' + body.__class__.__name__)

        self._event_listener.received_post_ack_listener(endpoint=endpoint, response_code=200, body_str=body)


def run_test_scenario(test_suite_identifier: str, test_identifier: str):
    config = get_configuration().testHarness

    harness = LLHarness(
        host=config.url,
        port=config.port,
        done_listener=None,
        done_listener_is_self=True)

    harness.load_test(test_suite_identifier=test_suite_identifier, test_identifier=test_identifier)
    ig.add_exit_handler(harness.stop)
    harness.start()
