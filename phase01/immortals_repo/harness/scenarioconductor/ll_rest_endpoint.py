import SocketServer
import json
import os
import re
import traceback

from data.validationresult import TestResult
from . import immortalsglobals as ig
from . import threadprocessrouter as tpr
from .data.adaptationresult import AdaptationResult
from .data.scenarioapiconfiguration import ScenarioConductorConfiguration
from .data.submissionresult import AdaptationStateContainer, SubmissionResult, ValidationStateContainer
from .packages import bottle, requests
from .reporting.abstractreporter import get_timestamp
from .scenarioconductor import ScenarioConductor
from .utils import path_helper as ph

_VALID_SESSION_IDENTIFIER_REGEX = "^[A-Za-z]+[A-Za-z0-9]*$"


# noinspection PyClassHasNoInit
class Status:
    PENDING = 'PENDING'
    NOT_APPLICABLE = 'NOT_APPLICABLE'
    RUNNING = 'RUNNING'
    SUCCESS = 'SUCCESS'
    FAILURE = 'FAILURE'


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
        self.route(path='/action/adaptAndValidateApplication', method='POST',
                   callback=self.adapt_and_validate_application)

        self.route(path='/action/validateBaselineApplication', method='POST',
                   callback=self.validate_baseline_application)

    def validate_baseline_application(self):

        if bottle.request.content_length > 0:
            ig.logger().error_das(
                'Validation of the baseline application in a perturbed environment is not yet supported!'
            )
            return bottle.HTTPResponse(status=400, body=None)

        env_conf = ScenarioConductorConfiguration.from_dict(
            json.load(open(ph(True, ig.IMMORTALS_ROOT, 'harness/scenarioconductor/configs/base_submission.json'))))
        app_conf = env_conf

        # noinspection PyTypeChecker
        sr = SubmissionResult(
            identifier=env_conf.sessionIdentifier,
            adaptation=AdaptationStateContainer(Status.NOT_APPLICABLE, None),
            validation=ValidationStateContainer(Status.PENDING, None)
        )

        # ig.logger().perturbation_detected(sr.to_dict())

        self.synthesis_history[env_conf.sessionIdentifier] = sr

        ig.logger().log_das_info(sr.to_dict())

        return_val = {
            'TIME': get_timestamp(),
            'RESULT': sr.to_dict()
        }

        tpr.start_thread(
            thread_method=_perform_validation,
            thread_args=[app_conf, env_conf, sr, 'baseline']
        )

        self._network_logger.write('TA SENDING ACK /action/adaptAndValidateApplication with BODY: ' +
                                   json.dumps(env_conf.to_dict(), indent=4, separators=(',', ': ')))

        return return_val

    def adapt_and_validate_application(self):
        # noinspection PyBroadException
        try:
            data_s = bottle.request.body.read()
            data_j = json.loads(data_s)
        except:
            ig.logger().error_das(
                'Error attempting to parse the submitted data as json.'
                '  Are you sure it is well-formed?. Details:\n\t' + traceback.format_exc())
            return bottle.HTTPResponse(status=400, body=None)

        self._network_logger.write(
            'TA RECEIVED POST /action/adaptAndValidateApplication with BODY: ' + json.dumps(data_j, indent=4,
                                                                                            separators=(
                                                                                                ',', ': ')))
        # noinspection PyBroadException
        try:
            s_conf = ScenarioConductorConfiguration.from_dict(data_j)
        except:
            # data contents error
            ig.logger().error_das(
                'Error attempting to parse the JSON data to an object.'
                ' Are you sure all fields are valid? Details:\n\t' + traceback.format_exc())
            return bottle.HTTPResponse(status=400, body=None)

        if not re.match(_VALID_SESSION_IDENTIFIER_REGEX, s_conf.sessionIdentifier):
            ig.logger().error_das(
                'Invalid sessionIdentifier. Are you sure it starts'
                ' with a letter and contains only letters and numbers?'
            )
            return bottle.HTTPResponse(status=400, body=None)

        # noinspection PyTypeChecker
        sr = SubmissionResult(
            identifier=s_conf.sessionIdentifier,
            adaptation=AdaptationStateContainer(Status.PENDING, None),
            validation=ValidationStateContainer(Status.PENDING, None)
        )

        self.synthesis_history[s_conf.sessionIdentifier] = sr

        ig.logger().perturbation_detected(sr.to_dict())
        ig.logger().log_das_info(sr.to_dict())

        return_val = {
            'TIME': get_timestamp(),
            'RESULT': sr.to_dict()
        }

        tpr.start_thread(
            thread_method=_perform_adaptation_and_validation,
            thread_args=[s_conf, s_conf, sr]
        )

        self._network_logger.write('TA SENDING ACK /action/adaptAndValidateApplication with BODY:'
                                   ' ' + json.dumps(data_j, indent=4, separators=(',', ': ')))

        return return_val


def _perform_adaptation_and_validation(app_conf, env_conf, result_container, scenario_template_tag='validation'):
    _perform_adaptation(
        app_conf=app_conf,
        result_container=result_container
    )
    if result_container.adaptation.status == Status.SUCCESS:
        _perform_validation(
            app_conf=app_conf,
            env_conf=env_conf,
            result_container=result_container,
            scenario_template_tag=scenario_template_tag
        )


def _perform_adaptation(app_conf, result_container):
    ig.logger().adapting(result_container.to_dict())
    execute_ttl_generation(app_conf)
    deployment_file = ph(True, ig.IMMORTALS_ROOT, 'models/scenario/deployment_model.ttl')
    ig.logger().artifact_file(deployment_file)

    payload = open(deployment_file, 'rb').read()
    headers = {'Content-Type': 'text/plain'}
    req = requests.post('http://localhost:8080/bbn/das/deployment-model', headers=headers, data=payload)
    ar = AdaptationResult.from_dict(json.loads(req.text))

    result_container.adaptation.details = ar
    result_container.adaptation.status = Status.SUCCESS if ar.adaptationStatusValue == 'SUCCESSFUL' else Status.FAILURE

    ig.logger().adaptation_completed(result_container.to_dict())
    ig.logger().log_das_info(result_container.to_dict())


# noinspection PyUnusedLocal
def _perform_validation(app_conf, env_conf, result_container, scenario_template_tag='validation'):
    # TODO: Add usage of env_conf!
    result_container.validation.status = Status.RUNNING
    ig.logger().log_das_info(result_container.to_dict())

    s_conductor = ScenarioConductor(
        scenario_configuration=app_conf,
        scenario_template_tag=scenario_template_tag,
        swallow_and_shutdown_on_exception=False)
    vr = s_conductor.execute()

    result_container.validation.details = vr

    st = Status.SUCCESS

    for r in vr.results:  # type: TestResult
        if r.currentState != 'PASSED':
            st = Status.FAILURE

    result_container.validation.status = st

    ig.logger().log_das_info(result_container.to_dict())


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
