import SocketServer
import json
import os
import re
import traceback

from data.validationresult import ValidationResult
from . import immortalsglobals as ig
from . import threadprocessrouter as tpr
from .data.adaptationresult import AdaptationResult
from .data.scenarioapiconfiguration import ScenarioConductorConfiguration
from .data.submissionresult import SubmissionResult
from .packages import bottle, requests
from .reporting.abstractreporter import get_timestamp
from .scenarioconductor import ScenarioConductor
from .utils import path_helper as ph

_VALID_SESSION_IDENTIFIER_REGEX = "^[A-Za-z]+[A-Za-z0-9]*$"


class LLRestEndpoint(bottle.Bottle):
    def __init__(self, host, port):
        super(LLRestEndpoint, self).__init__()
        self._define_routes()
        self.synthesis_history = {}

        self._network_logger = tpr.get_logging_endpoint(
            os.path.join(ig.configuration.artifactRoot, 'network_log.txt'))  # type: tpr.LoggingEndpoint

        SocketServer.TCPServer.allow_reuse_address = True
        SocketServer.ThreadingTCPServer.allow_reuse_address = True

        self.server = bottle.CherryPyServer(host=host, port=port)

    def start(self):
        bottle.run(app=self, server=self.server, debug=True)

    def stop(self):
        self.server.srv.stop()

    def _define_routes(self):
        self.route(path='/action/submitDeploymentModel', method='GET', callback=self._submit_deployment_model)

    def _submit_deployment_model(self):

        try:
            data_s = bottle.request.body.read()
            data_j = json.loads(data_s)
        except:
            ig.logger().error_das(
                'Error attempting to parse the submitted data as json.  Are you sure it is well-formed?. Details:\n\t' + traceback.format_exc())
            bottle.abort()
        else:

            self._network_logger.write(
                'TA RECEIVED POST /action/submitDeploymentModel with BODY: ' + json.dumps(data_j, indent=4,
                                                                                          separators=(
                                                                                              ',', ': ')))

            try:
                s_conf = ScenarioConductorConfiguration.from_dict(data_j)
            except:
                # data contents error
                ig.logger().error_das(
                    'Error attempting to parse the JSON data to an object. Are you sure all fields are valid? Details:\n\t' + traceback.format_exc())
                bottle.abort()
            else:

                if not re.match(_VALID_SESSION_IDENTIFIER_REGEX, s_conf.sessionIdentifier):
                    ig.logger().error_das(
                        "Invalid sessionIdentifier. Are you sure it starts with a letter and contains only letters and numbers?"
                    )
                    bottle.abort()

                else:
                    ig.logger().perturbation_detected("New deployment Model received.")
                    sr = SubmissionResult(
                        s_conf.sessionIdentifier,
                        False,
                        False,
                        None,
                        None
                    )

                    ig.logger().log_das_info(sr.to_dict())

                    self.synthesis_history[s_conf.sessionIdentifier] = sr

                    tpr.start_thread(thread_method=_submit_deployment_model_inner,
                                     thread_args=[s_conf, sr, 'submitDeploymentModel'],
                                     swallow_and_shutdown_on_exception=True)

                    return_val = {
                        'TIME': get_timestamp(),
                        'RESULT': sr.to_dict()
                    }

                    self._network_logger.write('TA SENDING ACK /action/submitDeploymentModel with BODY: ' +
                                               json.dumps(data_j, indent=4, separators=(',', ': ')))

                    return return_val


def _submit_deployment_model_inner(s_conf, sr, action):
    base_s_conf = ScenarioConductorConfiguration.from_dict(json.load(open(
        ph(True, ig.IMMORTALS_ROOT, 'harness/scenarioconductor/configs/base_submission.json'))))

    if base_s_conf.equals(s_conf):
        ar = AdaptationResult(
            "NOT_NECESSARY",
            [],
            '',
            'Adaptation is not necessary since the base configuration was provided',
            'None',
            s_conf.sessionIdentifier
        )
        sr.adaptationResult = ar
        sr.adaptationFinished = True

    else:
        execute_ttl_generation(s_conf)
        deployment_file = ph(True, ig.IMMORTALS_ROOT, 'models/scenario/deployment_model.ttl')
        ig.logger().artifact_file(deployment_file)
        ar = execute_das_submission(deployment_file)
        sr.adaptationResult = ar
        sr.adaptationFinished = True

    if ar.adaptationStatusValue == 'SUCCESSFUL' or ar.adaptationStatusValue == 'NOT_NECESSARY':
        d = sr.to_dict()
        ig.logger().submit_action(action, d)
        ig.logger().log_das_info(json.dumps(d))

        if ar.adaptationStatusValue == 'SUCCESSFUL':
            vr = execute_validation(s_conf)
        else:
            vr = execute_baseline_validation(s_conf)

        sr.validationResult = vr
        sr.validationFinished = True
        ig.logger().submit_action(action, sr.to_dict())
        ig.logger().log_das_info(json.dumps(sr.to_dict()))

    else:
        sr.validationFinished = True
        ig.logger().submit_action(action, sr.to_dict())
        ig.logger().log_das_info(json.dumps(sr.to_dict()))


def execute_das_submission(deployment_model_path):
    payload = open(deployment_model_path, 'rb').read()
    headers = {'Content-Type': 'text/plain'}
    req = requests.post('http://localhost:8080/bbn/das/deployment-model', headers=headers, data=payload)

    value_t = req.text
    value_d = json.loads(req.text)
    value_o = AdaptationResult.from_dict(value_d)

    return value_o


def execute_validation(scenario_configuration):
    """
    :rtype: ValidationResult
    """
    s_conductor = ScenarioConductor(
        scenario_configuration=scenario_configuration,
        scenario_template_tag='validation',
        swallow_and_shutdown_on_exception=False)
    result = s_conductor.execute()
    return result


def execute_baseline_validation(scenario_configuration):
    """
    :rtype: ValidationResult
    """
    s_conductor = ScenarioConductor(
        scenario_configuration=scenario_configuration,
        scenario_template_tag='baseline',
        swallow_and_shutdown_on_exception=False)
    result = s_conductor.execute()
    return result


def execute_ttl_generation(scenario_configuration):
    client = scenario_configuration.clients[0]
    res = client.presentResources

    params = [
        'python3',
        './py/mission_perturb.py',
        '--session',
        scenario_configuration.sessionIdentifier,
        '--output',
        'deployment_model.ttl',
        '--template',
        './template/gme-template.ttl',
        '--pli-client-msg-rate',
        str(60000 / int(client.latestSABroadcastIntervalMS)),
        '--image-client-msg-rate',
        str(60000 / int(client.latestSABroadcastIntervalMS)),
        '--server-bandwidth',
        str(scenario_configuration.server.bandwidth),
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
        'yes' if 'trustedLocations' in client.requiredProperties else 'no'
    ]

    tpr.check_output(args=params, cwd=ig.IMMORTALS_ROOT + 'models/scenario')
