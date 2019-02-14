import json
import re
import traceback
from threading import RLock
from typing import Tuple, List, Union

import bottle
import requests

from pymmortals import immortalsglobals as ig, threadprocessrouter as tpr
from pymmortals import triples_helper, websocket
from pymmortals.datatypes.deployment_model import LLP1Input
from pymmortals.datatypes.intermediary.challengeproblem import ChallengeProblem
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.routing import EventTags, EventTag, EventType
from pymmortals.datatypes.test_harness_api import LLTestActionSubmission, LLTestActionResult
from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat
from pymmortals.generated.mil.darpa.immortals.core.api.applications.applicationtype import ApplicationType
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.adaptationresult import AdaptationResult
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.adaptationstate import AdaptationState
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.status import Status
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testadapterstate import TestAdapterState
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.validationstate import ValidationState
from pymmortals.generated.mil.darpa.immortals.core.api.websockets.createapplicationinstancedata import \
    CreateApplicationInstanceData
from pymmortals.resources import resourcemanager as rm
from pymmortals.scenariorunner.scenariorunner import ScenarioRunner
from pymmortals.ttl_bridge import execute_ttl_generation
from pymmortals.utils import path_helper as ph
from . import th_client

_VALID_SESSION_IDENTIFIER_REGEX = "^[A-Za-z]+[A-Za-z0-9]*$"


def prettify_body(body: str) -> str:
    # noinspection PyBroadException,PyPep8
    try:
        return_val = json.dumps(json.loads(body), indent=4, separators=(',', ': '))
    except:
        return_val = body

    return return_val


def _parse_deployment_model(request_body: str, is_baseline: bool) \
        -> Union[Tuple[str, None], Tuple[None, GmeInterchangeFormat, LLP1Input]]:
    # noinspection PyBroadException,PyPep8
    try:
        data_dict = json.loads(request_body)

        # noinspection PyBroadException,PyPep8
        try:
            if is_baseline \
                    and ('ARGUMENTS' not in data_dict or data_dict['ARGUMENTS'] is None or data_dict[
                        'ARGUMENTS'] == ''):
                data_dict['ARGUMENTS'] = rm.get_phase1_input_dict('baseline')

            deployment_model = LLTestActionSubmission.from_dict(data_dict).ARGUMENTS

            if not re.match(_VALID_SESSION_IDENTIFIER_REGEX, deployment_model.sessionIdentifier):
                error_msg = ('Invalid sessionIdentifier. Are you sure it starts'
                             + ' with a letter and contains only letters and numbers?')
                return error_msg, None

            err = deployment_model.validate()
            if err is not None:
                return err, None

        except:
            # data contents error
            error_msg = ('Error attempting to parse the JSON data to an object.'
                         ' Are you sure all fields are valid and present? Details:\n\t' + traceback.format_exc())
            return error_msg, None

    except:
        error_msg = ('Error attempting to parse the submitted data as json. Are you sure it'
                     + ' is well-formed?. Details:\n\t' + traceback.format_exc())
        return error_msg, None

    return None, triples_helper.triplify_p1_input(input=deployment_model,
                                                  challenge_problem=ChallengeProblem.Phase01), deployment_model


class StateManager:
    def __init__(self):
        self._das_not_finished: bool = False
        self._scenario_runner_not_finished: bool = False
        self._lock: RLock() = RLock()
        self._deployment_model: GmeInterchangeFormat = None
        self._ll_p1_input: LLP1Input = None
        self._test_adapter_state: TestAdapterState = None
        self._scenario_template_tag: str = None
        self._raw_events: List(object) = []

    def get_deployment_model(self) -> GmeInterchangeFormat:
        return self._deployment_model

    def get_ll_p1_input(self) -> LLP1Input:
        return self._ll_p1_input

    def get_test_adapter_state(self) -> TestAdapterState:
        return self._test_adapter_state

    def get_scenario_template_tag(self) -> str:
        return self._scenario_template_tag

    def initialize_execution(self, initialization_data: str, perform_adaptation: bool,
                             scenario_template_tag: str) -> Tuple[str, None] or Tuple[None, str]:

        with self._lock:
            if not self.ready_for_submission():
                return ('A scenario with the identifier "' +
                        self._deployment_model.sessionIdentifier + ' is already running!')

            err_msg, dm, llp1input = _parse_deployment_model(initialization_data, True)

            if err_msg is not None:
                return err_msg, None

            self._das_not_finished = True
            self._scenario_runner_not_finished = True

            self._deployment_model = dm
            self._ll_p1_input = llp1input

            self._test_adapter_state: TestAdapterState = TestAdapterState(
                identifier=self._deployment_model.sessionIdentifier,
                adaptation=AdaptationState(
                    adaptationStatus=Status.PENDING if perform_adaptation else Status.NOT_APPLICABLE,
                    details=None
                ),
                validation=ValidationState(
                    executedTests=None,
                    overallIntentStatus=Status.PENDING
                ),
                rawLogData=[]
            )

            self._scenario_template_tag = scenario_template_tag

        ig.get_event_router().submit(EventTags.DeploymentModelLoaded, data=self._deployment_model)

        ig.get_event_router().submit(event_tag=EventTags.THStatusDasInfo,
                                     data=self._test_adapter_state)

        return_val = LLTestActionResult(RESULT=self._test_adapter_state)

        if perform_adaptation:
            ig.get_event_router().submit(event_tag=EventTags.THStatusPerturbationDetected,
                                         data=self._test_adapter_state)

            tpr.start_thread(
                thread_method=LLRestEndpoint.perform_adaptation_and_validation,
                thread_args=[self]
            )

        else:
            self._das_not_finished = False

            caid = CreateApplicationInstanceData(
                sessionIdentifier=self._ll_p1_input.sessionIdentifier,
                applicationType=ApplicationType.Client_ATAKLite
            )
            websocket.get_das_bridge().createApplicationInstance(caid)

            # copy the existing app to where it belongs!

            tpr.start_thread(
                thread_method=LLRestEndpoint.perform_validation,
                thread_args=[self]
            )

        return None, return_val.to_json_str(include_metadata=False)

    def ready_for_submission(self) -> bool:
        with self._lock:
            return (self._deployment_model is None and self._test_adapter_state is None
                    and not self._das_not_finished and not self._scenario_runner_not_finished)

    def set_das_running(self, value: bool):
        with self._lock:
            if self._test_adapter_state is None:
                raise Exception("OOPS")
            self._das_not_finished = value

        if not value:
            self._attempt_finish_submission()

    def set_scenario_runner_running(self, value: bool):
        with self._lock:
            if self._test_adapter_state is None:
                raise Exception("OOPS")

            self._scenario_runner_not_finished = value

        if not value:
            self._attempt_finish_submission()

    def log_raw_event(self, event: object):

        with self._lock:
            if self._test_adapter_state is not None:
                self._raw_events.append(event)

    previous_result = None

    def set_validation_data(self, data: ValidationState):
        with self._lock:
            if self._test_adapter_state is None:
                raise Exception('OOPS')
            elif self._test_adapter_state.validation.overallIntentStatus != Status.PENDING:
                raise Exception('OOPS')

            if StateManager.previous_result == data.overallIntentStatus:
                print('UHOH')

            StateManager.previous_result = data.overallIntentStatus
            self._test_adapter_state.validation = data

        self._attempt_finish_submission()

    def set_adaptation_result(self, result: AdaptationResult):
        with self._lock:
            if result.adaptationStatusValue == 'SUCCESSFUL':
                self._test_adapter_state.adaptation.adaptationStatus = Status.SUCCESS

            elif result.adaptationStatusValue == 'UNSUCCESSFUL':
                self._test_adapter_state.adaptation.adaptationStatus = Status.FAILURE

            elif result.adaptationStatusValue == 'ERROR':
                raise Exception('Error executing the adaptation on the das! details: ' + result.to_json_str_pretty())

            else:
                raise Exception('Unexpected adaptationStatusValue returned!' + result.to_json_str_pretty())

            self._test_adapter_state.adaptation.details = result

            self._das_not_finished = False

        if self._test_adapter_state.adaptation.adaptationStatus == Status.SUCCESS:
            ig.get_event_router().submit(event_tag=EventTags.THStatusAdaptationCompleted,
                                         data=self._test_adapter_state)

        elif self._test_adapter_state.adaptation.adaptationStatus == Status.FAILURE:
            ig.get_event_router().submit(event_tag=EventTags.THStatusMissionAborted,
                                         data=self._test_adapter_state)

        self._attempt_finish_submission()

    def _attempt_finish_submission(self):

        return_state = None

        with self._lock:
            if self._test_adapter_state is not None and not self._das_not_finished \
                    and not self._scenario_runner_not_finished \
                    and self._test_adapter_state.adaptation.adaptationStatus != Status.PENDING \
                    and self._test_adapter_state.validation.overallIntentStatus != Status.PENDING:
                self._test_adapter_state.rawLogData = self._raw_events
                return_state = self._test_adapter_state
                self._das_not_finished = False
                self._scenario_runner_not_finished = False
                self._deployment_model = None
                self._ll_p1_input = None
                self._test_adapter_state = None
                self._scenario_template_tag = None
                self._raw_events = []

        if return_state is not None:
            ig.get_event_router().submit(event_tag=EventTags.THSubmitDone,
                                         data=return_state)


class LLRestEndpoint:
    def __init__(self):
        ig.get_olympus().route_add(path='/action/adaptAndValidateApplication', method='POST',
                                   callback=self.adapt_and_validate_application)

        ig.get_olympus().route_add(path='/action/validateBaselineApplication', method='POST',
                                   callback=self.validate_baseline_application)

        ig.get_event_router().subscribe_listener(event_tag_or_type=EventType.ERROR, listener=th_client.process_error)
        ig.get_event_router().subscribe_listener(event_tag_or_type=EventType.STATUS, listener=th_client.process_status)
        ig.get_event_router().set_log_events_to_file(event_tag_or_type=EventType.ERROR,
                                                     filepath=get_configuration().logFile,
                                                     transformer=th_client.THErrorStringTransformer)
        ig.get_event_router().subscribe_listener(event_tag_or_type=EventTags.ValidationTestsFinished,
                                                 listener=self.receive_validation_finished)

        if get_configuration().testAdapter.reportRawData:
            ig.get_event_router().subscribe_listener(
                event_tag_or_type=EventTags.AnalyticsEventServerNetworkTrafficMeasuredBytes,
                listener=self._raw_event_appender)

            ig.get_event_router().subscribe_listener(
                event_tag_or_type=EventTags.AnalyticsEventServerNetworkTrafficCalculatedBytesPerSec,
                listener=self._raw_event_appender)

        self._sm = StateManager()

    # noinspection PyUnusedLocal
    def _raw_event_appender(self, event_tag: EventTag, data: object):
        self._sm.log_raw_event(data)

    # noinspection PyMethodMayBeStatic
    def start(self):
        ig.get_olympus()

    def validate_baseline_application(self):

        body = bottle.request.body.read()

        if isinstance(body, bytes):
            body = body.decode()

        print('##' + body)

        ig.get_event_router().submit(EventTags.NetworkAcknowledgedPost,
                                     'TA RECEIVED POST /action/validateBaselineApplication with BODY: ' +
                                     prettify_body(body))

        error_string, return_val = self._sm.initialize_execution(bottle.request.body.read(), False, 'baseline')

        if error_string is not None:
            ig.get_event_router().submit(EventTags.THErrorGeneral, error_string)
            ig.get_event_router().submit(EventTags.NetworkAcknowledgedPost,
                                         'TA SENDING ACK /action/adaptAndValidateApplication of 400 with body: ' +
                                         error_string)
            return bottle.HTTPResponse(
                status=400,
                body=error_string
            )

        else:
            ig.get_event_router().submit(EventTags.NetworkAcknowledgedPost,
                                         'TA SENDING ACK /action/validateBaselineApplication with BODY: ' +
                                         return_val)

        return return_val

    def adapt_and_validate_application(self):

        body = bottle.request.body.read()

        if isinstance(body, bytes):
            body = body.decode()

        print('##' + body)

        ig.get_event_router().submit(EventTags.NetworkAcknowledgedPost,
                                     'TA RECEIVED POST /action/adaptAndValidateApplication with BODY: ' +
                                     prettify_body(body))

        error_string, return_val = self._sm.initialize_execution(bottle.request.body.read(), True, 'validation')

        if error_string is not None:
            ig.get_event_router().submit(EventTags.THErrorGeneral, error_string)
            ig.get_event_router().submit(EventTags.NetworkAcknowledgedPost,
                                         'TA SENDING ACK /action/adaptAndValidateApplication of 400 with body: ' +
                                         error_string)
            return bottle.HTTPResponse(
                status=400,
                body=error_string
            )

        else:
            ig.get_event_router().submit(EventTags.NetworkAcknowledgedPost,
                                         'TA SENDING ACK /action/adaptAndValidateApplication with BODY: ' +
                                         return_val)

        return return_val

    @staticmethod
    def perform_adaptation_and_validation(state_manager: StateManager):
        LLRestEndpoint.perform_adaptation(state_manager=state_manager)

        if state_manager.get_test_adapter_state().adaptation.adaptationStatus == Status.SUCCESS:
            LLRestEndpoint.perform_validation(state_manager=state_manager)

    @staticmethod
    def perform_adaptation(state_manager: StateManager):
        ig.get_event_router().submit(event_tag=EventTags.THStatusAdapting,
                                     data=state_manager.get_test_adapter_state())

        # TODO: This should be done outside of the FredScript, but requires the DAS supporting JSON triples
        execute_ttl_generation(state_manager.get_ll_p1_input())
        deployment_file = ph(True, get_configuration().immortalsRoot, 'models/scenario/deployment_model.ttl')

        ig.get_event_router().archive_file(deployment_file)

        payload = open(deployment_file, 'rb').read()
        headers = {'Content-Type': 'text/plain'}
        req = requests.post('http://localhost:8080/bbn/das/deployment-model', headers=headers, data=payload)

        ar = AdaptationResult.from_dict(json.loads(req.text))

        state_manager.set_adaptation_result(ar)

    @staticmethod
    def perform_validation(state_manager: StateManager):
        state_manager.set_scenario_runner_running(True)

        src = triples_helper.load_phase01_scenario_runner_configuration(state_manager.get_deployment_model())

        ig.get_event_router().archive_to_file(
            str_to_write=src.to_json_str_pretty(include_metadata=False),
            target_subpath=src.sessionIdentifier + '-scenariorunnerconfiguration.json',
            clobber_existing=True)

        s_runner = ScenarioRunner(runner_configuration=src, deployment_model=state_manager.get_deployment_model())
        s_runner.execute_scenario()

        state_manager.set_scenario_runner_running(False)

    # noinspection PyUnusedLocal
    def receive_validation_finished(self, event_tag, data):
        self._sm.set_validation_data(data)
