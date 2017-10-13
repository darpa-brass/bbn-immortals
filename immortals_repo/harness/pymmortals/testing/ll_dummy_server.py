#!/usr/bin/env python
import os
import socketserver
import time
from typing import Dict, Union

import bottle
from cherrypy import wsgiserver

from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.testing import Phase2TestHarnessListenerInterface
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.testharnessendpoint import TestHarnessEndpoint
from . import ta_submitter as tas


class ImmortalsLLServer(bottle.ServerAdapter):
    def __init__(self, host='127.0.0.1', port=8080, **config):
        super().__init__(host, port, **config)
        self.server: wsgiserver.CherryPyWSGIServer = None

    def run(self, handler):  # pragma: no cover
        self.server = wsgiserver.CherryPyWSGIServer((self.host, self.port), handler)
        try:
            self.server.start()
        finally:
            self.server.stop()

    def stop(self):
        self.server.stop()


class LLHarness(bottle.Bottle):
    def __init__(self, host: str, port: int, logger: Phase2TestHarnessListenerInterface):
        super(LLHarness, self).__init__()

        self._logger = logger
        tas.set_logger(self._logger)

        self._define_routes()

        self._results = []

        socketserver.TCPServer.allow_reuse_address = True
        socketserver.ThreadingTCPServer.allow_reuse_address = True

        self.server = ImmortalsLLServer(host=host, port=port)

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

    def stop(self):
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
        self._logger.receiving_post_listener(TestHarnessEndpoint.ERROR, body_str)
        self._set_success_response(endpoint=TestHarnessEndpoint.ERROR)

    def _ready(self):
        body_str = bottle.request.body.read().decode()

        time.sleep(.2)
        self._logger.receiving_post_listener(TestHarnessEndpoint.READY, body_str)
        self._set_success_response(endpoint=TestHarnessEndpoint.READY)

    def _status(self):
        body_str = bottle.request.body.read().decode()

        time.sleep(.2)
        self._logger.receiving_post_listener(TestHarnessEndpoint.STATUS, body_str)
        self._set_success_response(endpoint=TestHarnessEndpoint.STATUS)

    def _done(self):
        body_str = bottle.request.body.read().decode()

        time.sleep(.2)
        self._logger.receiving_post_listener(TestHarnessEndpoint.DONE, body_str)
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

        self._logger.received_post_ack_listener(endpoint=endpoint, response_code=200, body_str=body)
