#!/usr/bin/env python
import SocketServer
import argparse
import json
import logging

from .packages import bottle
from .packages import requests

TH_PORT = 80
TH_URL = 'brass-th'
TH_PROTOCOL = 'http://'

TA_PORT = 80
TA_URL = 'brass-ta'
TA_PROTOCOL = 'http://'

URL_TEMPLATE = TA_PROTOCOL + TA_URL + ':' + str(TA_PORT) + '/{path}'

JSON_HEADERS = {'Content-Type': 'application/json'}

__handler = logging.FileHandler('ll_harness_log.txt')
__handler.setFormatter(logging.Formatter('%(message)s'))
_network_logger = logging.getLogger('ll_harness_log.txt')
_network_logger.setLevel(logging.INFO)
_network_logger.addHandler(__handler)

parser = argparse.ArgumentParser(description='IMMORTALS Mock LL TH')
parser.add_argument('-thp', '--test-harness-port', type=int)
parser.add_argument('-tha', '--test-harness-address', type=str)
parser.add_argument('-tap', '--test-adapter-port', type=int)
parser.add_argument('-taa', '--test-adapter-address', type=str)


class LLHarness(bottle.Bottle):
    def __init__(self, host, port):
        super(LLHarness, self).__init__()
        self._define_routes()

        SocketServer.TCPServer.allow_reuse_address = True
        SocketServer.ThreadingTCPServer.allow_reuse_address = True

        self.server = bottle.CherryPyServer(host=host, port=port)

    def start(self):
        bottle.run(app=self, server=self.server, debug=True)

    def shutdown(self):
        self.server.srv.shutdown()

    def _define_routes(self):
        self.route(path='/error', method='POST', callback=self._error)
        self.route(path='/ready', method='POST', callback=self._ready)
        self.route(path='/status', method='POST', callback=self._status)
        self.route(path='/action/<target>', method='POST', callback=self._action)

    @staticmethod
    def _error():
        obj_j = bottle.request.json
        _network_logger.info(
            'TH RECEIVED POST /error with BODY: ' + json.dumps(obj_j, indent=4, separators=(',', ': ')) + '\n')

    @staticmethod
    def _ready():
        obj_j = bottle.request.json
        _network_logger.info(
            'TH RECEIVED POST /ready with BODY: ' + json.dumps(obj_j, indent=4, separators=(',', ': ')) + '\n')

        obj_s = open('sample_submission.json', 'r').read()

        _network_logger.info('TH SENDING POST /action/adaptAndValidateApplication\n')
        url = URL_TEMPLATE.format(path='action/adaptAndValidateApplication')
        r = requests.post(url=url,
                          headers=JSON_HEADERS,
                          data=obj_s
                          )

        # _network_logger.info('TH SENDING POST /action/validateBaselineApplication\n')
        # url = URL_TEMPLATE.format(path='action/validateBaselineApplication')
        # r = requests.post(url=url)

        ack_body = r.text
        log_body = None

        try:
            log_body = json.dumps(json.loads(ack_body), indent=4, separators=(',', ': '))
        except:
            pass

        if log_body is None:
            log_body = ack_body

        _network_logger.info('TH RECEIVED ACK for POST /action/adaptAndValidateApplication of ' + str(r.status_code) +
                             ' with BODY: ' + log_body + '\n')

    @staticmethod
    def _status():
        obj_j = bottle.request.json
        _network_logger.info(
            'TH RECEIVED POST /status with BODY: ' + json.dumps(obj_j, indent=4, separators=(',', ': ')) + '\n')

    @staticmethod
    def _action(target):
        obj_j = bottle.request.json
        _network_logger.info('TH RECEIVED POST /action/' + target + ' with BODY: ' + json.dumps(obj_j, indent=4,
                                                                                                separators=(
                                                                                                    ',', ': ')) + '\n')


def main():
    global TH_PORT, TH_URL, TA_PORT, TA_URL, URL_TEMPLATE
    args = parser.parse_args()

    if args.test_harness_port is not None:
        TH_PORT = args.test_harness_port

    if args.test_harness_address is not None:
        TH_URL = args.test_harness_address

    if args.test_adapter_port is not None:
        TA_PORT = args.test_adapter_port

    if args.test_adapter_address is not None:
        TA_URL = args.test_adapter_address

    URL_TEMPLATE = TA_PROTOCOL + TA_URL + ':' + str(TA_PORT) + '/{path}'

    o = LLHarness(host=TH_URL, port=TH_PORT)

    o.start()


if __name__ == '__main__':
    main()
