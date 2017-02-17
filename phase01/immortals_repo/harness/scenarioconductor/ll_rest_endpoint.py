import json
import os
import re
import traceback

import bottle

from data.base.adaptationresult import AdaptationResult
from data.base.scenarioapiconfiguration import ScenarioConductorConfiguration
from data.base.validationresults import ValidationResults
from packages import requests
from . import immortalsglobals as ig
from . import threadprocessrouter as tpr
from . import validation
from .data.base.root_configuration import demo_mode
from .data.base.tools import path_helper as ph
from .ll_api.data import AdaptationState, Status, TestAdapterState, ValidationState, TestDetails
from .reporting.abstractreporter import get_timestamp
from .scenarioconductor import ScenarioConductor

_VALID_SESSION_IDENTIFIER_REGEX = "^[A-Za-z]+[A-Za-z0-9]*$"


class LLRestEndpoint():
    def __init__(self, url, port):
        ig.get_olympus().add_call(path='/action/adaptAndValidateApplication', method='POST',
                                  callback=self.adapt_and_validate_application)

        ig.get_olympus().add_call(path='/action/validateBaselineApplication', method='POST',
                                  callback=self.validate_baseline_application)

        self.synthesis_history = {}

        self._network_logger = tpr.get_logging_endpoint(
            os.path.join(ig.configuration.artifactRoot, 'network_log.txt'))  # type: tpr.LoggingEndpoint

    def start(self):
        ig.start_olympus()

    def validate_baseline_application(self):
        # noinspection PyBroadException
        data_s = bottle.request.body.read()

        # noinspection PyBroadException
        try:
            data_s = bottle.request.body.read()
            data_j = json.loads(data_s)
            self._network_logger.write(
                'TA RECEIVED POST /action/validateBaselineApplication with BODY: ' + json.dumps(data_j, indent=4,
                                                                                                separators=(
                                                                                                    ',', ': ')))
        except:
            ig.logger().error_das(
                'Error attempting to parse the submitted data as json.'
                '  Are you sure it is well-formed?. Details:\n\t' + traceback.format_exc())
            return bottle.HTTPResponse(status=400, body=None)

        if 'ARGUMENTS' not in data_j or data_j['ARGUMENTS'] is None or data_j['ARGUMENTS'] == '':
            if demo_mode:
                env_conf = ScenarioConductorConfiguration.from_dict(
                    json.load(open(
                        ph(True, ig.IMMORTALS_ROOT,
                           'harness/scenarioconductor/configs/samples/scenarioconfiguration-baseline-a-demo.json'))))

            else:
                env_conf = ScenarioConductorConfiguration.from_dict(
                    json.load(open(
                        ph(True, ig.IMMORTALS_ROOT,
                           'harness/scenarioconductor/configs/samples/scenarioconfiguration-baseline.json'))))

        else:
            # noinspection PyBroadException
            try:
                env_conf = ScenarioConductorConfiguration.from_dict(data_j['ARGUMENTS'])
            except:
                # data contents error
                ig.logger().error_das(
                    'Error attempting to parse the JSON data to an object.'
                    ' Are you sure all fields are valid? Details:\n\t' + traceback.format_exc())
                return bottle.HTTPResponse(status=400, body=None)

            if not re.match(_VALID_SESSION_IDENTIFIER_REGEX, env_conf.sessionIdentifier):
                ig.logger().error_das(
                    'Invalid sessionIdentifier. Are you sure it starts'
                    ' with a letter and contains only letters and numbers?'
                )
                return bottle.HTTPResponse(status=400, body=None)

        if demo_mode:
            ig.get_olympus().demo.load_scenario_configuration(scenario_configuration=env_conf)
        # noinspection PyTypeChecker
        sr = TestAdapterState(
            identifier=env_conf.sessionIdentifier,
            adaptation=AdaptationState(
                adaptationStatus=Status.NOT_APPLICABLE,
                details=None
            ),
            validation=ValidationState(
                executedTests=None,
                overallIntentStatus=Status.PENDING
            ),
            rawLogData=None
        )

        self.synthesis_history[env_conf.sessionIdentifier] = sr

        ig.logger().log_das_info(sr.to_dict())

        return_val = {
            'TIME': get_timestamp(),
            'RESULT': sr.to_dict()
        }

        tpr.start_thread(
            thread_method=_perform_validation,
            thread_args=[env_conf, sr, 'baseline']
        )

        self._network_logger.write('TA SENDING ACK /action/validateBaselineApplication with BODY: ' +
                                   json.dumps(return_val, indent=4, separators=(',', ': ')))

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
            s_conf = ScenarioConductorConfiguration.from_dict(data_j['ARGUMENTS'])
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

        if demo_mode:
            ig.get_olympus().demo.load_scenario_configuration(scenario_configuration=s_conf)

        # noinspection PyTypeChecker
        sr = TestAdapterState(
            identifier=s_conf.sessionIdentifier,
            adaptation=AdaptationState(
                adaptationStatus=Status.PENDING,
                details=None
            ),
            validation=ValidationState(
                executedTests=None,
                overallIntentStatus=Status.PENDING
            ),
            rawLogData=None
        )

        self.synthesis_history[s_conf.sessionIdentifier] = sr

        ig.logger().perturbation_detected(message=sr.to_dict(),
                                          display_message="Perturbation of the Environment has been detected")
        ig.logger().log_das_info(sr.to_dict())

        return_val = {
            'TIME': get_timestamp(),
            'RESULT': sr.to_dict()
        }

        tpr.start_thread(
            thread_method=_perform_adaptation_and_validation,
            thread_args=[s_conf, sr]
        )

        self._network_logger.write('TA SENDING ACK /action/adaptAndValidateApplication with BODY:'
                                   ' ' + json.dumps(return_val, indent=4, separators=(',', ': ')))

        return return_val


def _perform_adaptation_and_validation(env_conf, result_container, scenario_template_tag='validation'):
    _perform_adaptation(
        app_conf=env_conf,
        result_container=result_container
    )
    if result_container.adaptation.adaptationStatus == Status.SUCCESS:
        _perform_validation(
            env_conf=env_conf,
            result_container=result_container,
            scenario_template_tag=scenario_template_tag
        )


def _perform_adaptation(app_conf, result_container):
    ig.logger().adapting(message=result_container.to_dict(), display_message="Beginning adaptation")
    execute_ttl_generation(app_conf)
    deployment_file = ph(True, ig.IMMORTALS_ROOT, 'models/scenario/deployment_model.ttl')
    ig.logger().artifact_file(deployment_file)

    payload = open(deployment_file, 'rb').read()
    headers = {'Content-Type': 'text/plain'}
    req = requests.post('http://localhost:8080/bbn/das/deployment-model', headers=headers, data=payload)

    ar = AdaptationResult.from_dict(json.loads(req.text))

    result_container.adaptation.details = ar

    if ar.adaptationStatusValue == 'SUCCESSFUL':
        result_container.adaptation.adaptationStatus = Status.SUCCESS
        ig.logger().adaptation_completed(result_container.to_dict(), display_message="Adaptation completed")
        ig.logger().log_das_info(result_container.to_dict())

    else:
        result_container.adaptation.adaptationStatus = Status.FAILURE
        ig.logger().log_das_info(result_container.to_dict())
        ig.logger().mission_aborted(result_container.to_dict())


# noinspection PyUnusedLocal
def _perform_validation(env_conf, result_container, scenario_template_tag='validation'):
    """
    :type env_conf: ScenarioConductorConfiguration
    :type result_container: TestAdapterState
    :type scenario_template_tag: str
    :rtype: ValidationState
    """

    result_container.validation.status = Status.RUNNING
    ig.logger().log_das_info(result_container.to_dict())

    s_conductor = ScenarioConductor(
        scenario_configuration=env_conf,
        scenario_template_tag=scenario_template_tag,
        swallow_and_shutdown_on_exception=False)
    vr = s_conductor.execute()

    result_container.validation.details = vr

    vs = process_results(vr, env_conf)

    result_container.validation = vs
    result_container.rawLogData = s_conductor.sr.behaviorvalidator.ll_events

    ig.logger().log_das_info(result_container.to_dict())
    ig.logger().done(message=result_container.to_dict(),
                     display_message="The scenario has been completed")


def process_results(validation_results, scenario_configuration):
    """
    :type validation_results: ValidationResults
    :type scenario_configuration: ScenarioConductorConfiguration
    :rtype: ValidationState
    """

    # Calculate the validators
    validators = validation.calculate_validators(scenario_configuration)

    # Transform the details returned from the validation server to a mapping of test identifiers to the details
    # formatted for LL consumption
    test_result_map = {
        k.validatorIdentifier:
            TestDetails(
                testIdentifier=k.validatorIdentifier,
                expectedStatus=(
                    Status.NOT_APPLICABLE if k.validatorIdentifier not in validators
                    else Status.SUCCESS if validators[k.validatorIdentifier] else Status.FAILURE
                ),
                actualStatus=k.currentState,
                details=k
            )
        for k in validation_results.results}

    overall_pass = True

    for validator in test_result_map.keys():
        td = test_result_map[validator]  # type: TestDetails

        if td.expectedStatus != td.actualStatus:
            if td.expectedStatus is Status.SUCCESS or td.expectedStatus is Status.FAILURE:
                overall_pass = False
                break

    return ValidationState(
        executedTests=test_result_map.values(),
        overallIntentStatus=Status.SUCCESS if overall_pass else Status.FAILURE
    )


def execute_ttl_generation(scenario_configuration, output_file=None):
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
        str(60000 / int(client.imageBroadcastIntervalMS)),
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

    if output_file is not None:
        params.append('--output')
        params.append(os.path.abspath(output_file))

    tpr.check_output(args=params, cwd=ig.IMMORTALS_ROOT + 'models/scenario')
