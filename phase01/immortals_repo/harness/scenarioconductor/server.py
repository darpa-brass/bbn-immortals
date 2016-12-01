#!/usr/bin/env python
import SocketServer
import argparse
import json
import time

import immortalsglobals
import threadprocessrouter as tpr
from data.adaptationresult import AdaptationResult
from data.scenarioconfiguration import ScenarioConfiguration
from data.submissionresult import SubmissionResult
from packages import bottle

# from packages.bottle import get, post
from packages import requests
from scenarioconductor import ScenarioConductor

parser = argparse.ArgumentParser()
parser.add_argument('--port', required=True, type=int, metavar='PORT')


def _convert_to_mission_perturb_parameters(
        input_configuration
):
    c = input_configuration  # type: ScenarioConfiguration

    client = c.clients[0]
    res = client.present_resources

    return [
        'python3',
        './py/mission_perturb.py',
        '--session',
        c.session_identifier,
        '--output',
        'deployment_model.ttl',
        '--template',
        './template/gme-template.ttl',
        '--pli-client-msg-rate',
        str(60000 / int(client.latestsa_broadcast_interval_ms)),
        '--image-client-msg-rate',
        str(60000 / int(client.latestsa_broadcast_interval_ms)),
        '--server-bandwidth',
        str(input_configuration.server.bandwidth),
        '--client-device-count',
        str(client.count),
        '--android-bluetooth-resource',
        'yes' if 'bluetooth' in res else 'no',
        '--android-usb-resource',
        'yes' if 'usb' in res else 'no',
        '--android-internal-gps-resource',
        'yes' if 'internalGps' in res else 'no',
        '--android-ui-resource',
        'yes' if 'userInterface' in res else 'no',
        '--gps-satellite-resource',
        'yes' if 'gpsSatellites' in res else 'no',
        '--mission-trusted-comms',
        'yes' if 'trustedLocations' in client.required_properties else 'no'
    ]


class Server(bottle.Bottle):
    def __init__(self, host, port):
        super(Server, self).__init__()
        self._define_routes()
        self.synthesis_history = {}

        SocketServer.TCPServer.allow_reuse_address = True
        SocketServer.ThreadingTCPServer.allow_reuse_address = True

        self.server = bottle.WSGIRefServer(host=host, port=port)

    def start(self):
        bottle.run(app=self, server=self.server)

    def shutdown(self):
        print "SHUDDOWN"
        self.server.srv.shutdown()

    def _define_routes(self):
        self.route(path='/submit', method='POST', callback=self._submit)
        self.route(path='/query/<session_identifier>', method='GET', callback=self._query)

    def _submit(self):
        request_j = bottle.request.json

        s_conf = ScenarioConfiguration.from_dict(request_j)

        sr = SubmissionResult(
                s_conf.session_identifier,
                False,
                False,
                None,
                None
        )
        self.synthesis_history[s_conf.session_identifier] = sr

        Server._execute_ttl_generation(s_conf)
        ar = Server._execute_das_submission(immortalsglobals.IMMORTALS_ROOT + 'models/scenario/deployment_model.ttl')  # type: AdaptationResult
        sr.adaptationResult = ar
        sr.synthesisFinished = True

        if ar.adaptationStatusValue == 'SUCCESSFUL':
            tpr.start_thread(thread_method=Server._execute_validation, thread_args=[self, s_conf])

        else:
            sr.validationFinished = True

        return json.dumps(sr.to_dict())

    def _query(self, session_identifier):
        return json.dumps(self.synthesis_history[session_identifier].to_dict())

    @staticmethod
    def _execute_ttl_generation(scenario_configuration):
        params = _convert_to_mission_perturb_parameters(scenario_configuration)
        tpr.check_output(args=params, cwd=immortalsglobals.IMMORTALS_ROOT + 'models/scenario')

    @staticmethod
    def _execute_das_submission(deployment_model_path):
        payload = open(deployment_model_path, 'rb').read()
        headers = {'Content-Type': 'text/plain'}
        req = requests.post('http://localhost:8080/bbn/das/deployment-model', headers=headers, data=payload)

        return AdaptationResult.from_dict(json.loads(req.text))

    def _execute_validation(self, scenario_configuration):
        s_conductor = ScenarioConductor(scenario_configuration)
        result = s_conductor.execute()
        sr = self.synthesis_history[scenario_configuration.session_identifier]
        sr.validationResult = result
        sr.validationFinished = True


def main():
    args = parser.parse_args()
    immortalsglobals.main_thread_cleanup_hookup()
    o = Server(host='0.0.0.0', port=args.port)

    tpr.start_thread(thread_method=Server.start, thread_args=[o], shutdown_method=Server.shutdown, shutdown_args=[o])

    while tpr.keep_running():
        time.sleep(1)


if __name__ == '__main__':
    main()
