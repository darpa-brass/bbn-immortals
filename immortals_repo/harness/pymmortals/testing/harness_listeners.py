import json
import logging
import sys
from threading import RLock
from typing import Dict, Union, List, Callable, Optional

import pymmortals.threadprocessrouter as tpr
from pymmortals.datatypes.serializable import Serializable
from pymmortals.datatypes.testing import Phase2TestHarnessListenerInterface, Phase2SubmissionFlow, Phase2TestScenario
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.testadapterendpoint import TestAdapterEndpoint
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.testharnessendpoint import TestHarnessEndpoint
from pymmortals.resources import resourcemanager
from pymmortals.testing.ta_submitter import produce_test_adapter_submissions, TestAdapterSubmission

keep_going = True


def immortals_assert(assertion: bool, msg: Optional[str] = None):
    global keep_going
    if not assertion:
        keep_going = False
        assert assertion, msg


def _get_activity_msg(endpoint: Union[TestAdapterEndpoint, TestHarnessEndpoint],
                      body_val: Union[Dict, str, None] = None, status_code: int = None):
    msg = ''

    if isinstance(endpoint, TestAdapterEndpoint):
        sender = 'TH'
        receiver = 'TA'

    elif isinstance(endpoint, TestHarnessEndpoint):
        sender = 'TA'
        receiver = 'TH'

    else:
        raise Exception('Unexpected endpoint type "' + endpoint.__class__.name + '"!')

    if status_code is None:
        msg += (sender + ' ' + endpoint.restType.name + ' to ' + receiver + endpoint.path)
    else:
        msg += (receiver + ' ACK to ' + sender + ' ' + endpoint.restType.name + ' to ' + receiver + endpoint.path)

    if body_val is None or body_val == 'null' or body_val == '':
        msg += ' with no BODY'

    else:
        msg += (' with BODY:\n' + prettify(body=body_val))

    return msg


def prettify(body: Union[Serializable, Dict, str, None]) -> Union[str, None]:
    return_body = None

    if body is None or body == '':
        return_body = ''

    if isinstance(body, Serializable):
        return_body = body.to_json_str_pretty()

    elif isinstance(body, dict):
        return_body = json.dumps(body, indent=4, separators=(',', ': '))

    elif isinstance(body, str):
        # noinspection PyBroadException
        try:
            return_body = json.dumps(json.loads(body), indent=4, separators=(',', ': '))
        except:
            return_body = body

    return return_body


class NetworkExpectationValidator:
    def __init__(self,
                 endpoint: Union[TestHarnessEndpoint, TestAdapterEndpoint],
                 submission_values: Union[Dict, str, None],
                 ack_values: Union[Dict, str, None],
                 ack_code: int):
        self.endpoint = endpoint
        self.submission_values = submission_values
        self.ack_values = ack_values
        self.ack_code = ack_code
        self.submission_validated = False
        self.ack_validated = False

    def validate_submission(self, endpoint: Union[TestHarnessEndpoint, TestAdapterEndpoint],
                            body_str: str):
        immortals_assert(not self.submission_validated)
        immortals_assert(not self.ack_validated)

        immortals_assert(endpoint == self.endpoint,
                         'Endpoint ' + endpoint.name + ' != ' + str(self.endpoint.name))

        self._validate_values(body_str=body_str, expected_values=self.submission_values)

        self.submission_validated = True

    def validate_ack(self, endpoint: Union[TestHarnessEndpoint, TestAdapterEndpoint],
                     body_str: str, status_code: int = None):

        immortals_assert(self.submission_validated)
        immortals_assert(not self.ack_validated)

        immortals_assert(endpoint == self.endpoint, \
                         ('Endpoint ' + endpoint.name + ' != ' + str(self.endpoint.name)))

        immortals_assert(self.ack_code is not None)
        immortals_assert(self.ack_code == status_code, \
                         ('Status code ' + str(status_code) + ' != ' + str(self.ack_code)))

        self._validate_values(body_str=body_str, expected_values=self.ack_values)

        self.ack_validated = True

    def finished(self) -> bool:
        return self.submission_validated and self.ack_validated

    @staticmethod
    def _validate_values(body_str: str, expected_values: Dict[str, str]):
        if expected_values is not None:
            for key in expected_values:
                target_d = json.loads(body_str)

                override_path = key.split('.')
                override_tail = override_path.pop()

                for path_element in override_path:
                    value = target_d[path_element]
                    target_d = value

                immortals_assert(target_d[override_tail] == expected_values[key], \
                                 ('Value of "' + str(target_d[override_tail]) + '" for "' + key
                                  + '" Does not match expected value of "' + str(expected_values[key]) + '"!'))


def generate_expected_activity(test_scenario: Phase2TestScenario) -> List[NetworkExpectationValidator]:
    flow = test_scenario.submissionFlow

    validators: List[NetworkExpectationValidator] = list()

    validators.append(
        NetworkExpectationValidator(
            endpoint=TestHarnessEndpoint.READY,
            submission_values=None,
            ack_values=None,
            ack_code=200
        )
    )

    if flow == Phase2SubmissionFlow.BaselineA or flow == Phase2SubmissionFlow.BaselineB:
        validators.append(
            NetworkExpectationValidator(
                endpoint=TestAdapterEndpoint.ENABLED,
                submission_values={
                    "dasEnabled": False
                },
                ack_values=None,
                ack_code=200
            ),
        )

    if flow == Phase2SubmissionFlow.BaselineA or flow == Phase2SubmissionFlow.BaselineB:
        validators.append(
            NetworkExpectationValidator(
                endpoint=test_scenario.perturbationScenario.endpoint,
                submission_values=None,
                ack_values={
                    "adaptation.adaptationStatus": "NOT_APPLICABLE",
                    "validation.verdictOutcome": "RUNNING"
                },
                ack_code=200
            )
        )

        validators.append(
            NetworkExpectationValidator(
                endpoint=TestHarnessEndpoint.DONE,
                submission_values={
                    "adaptation.adaptationStatus": "NOT_APPLICABLE",
                    "validation.verdictOutcome": "PASS"
                },
                ack_values=None,
                ack_code=200
            )
        )

    elif flow == Phase2SubmissionFlow.Challenge:
        validators.append(
            NetworkExpectationValidator(
                endpoint=test_scenario.perturbationScenario.endpoint,
                submission_values=None,
                ack_values={
                    "adaptation.adaptationStatus": "RUNNING",
                    "validation.verdictOutcome": "PENDING"
                },
                ack_code=200
            )
        )

        validators.append(
            NetworkExpectationValidator(
                endpoint=TestHarnessEndpoint.STATUS,
                submission_values={
                    "adaptation.adaptationStatus": "SUCCESS",
                    "validation.verdictOutcome": "RUNNING"
                },
                ack_values=None,
                ack_code=200
            )
        )

        validators.append(
            NetworkExpectationValidator(
                endpoint=TestHarnessEndpoint.DONE,
                submission_values={
                    "adaptation.adaptationStatus": "SUCCESS",
                    "validation.verdictOutcome": "PASS"
                },
                ack_values=None,
                ack_code=200
            )
        )

    else:
        raise Exception('Unsupported SubmissionFlow "' + flow.name + '"!')

    return validators


class ScenarioExecution(Phase2TestHarnessListenerInterface):
    """
    :type finished: bool
    :type test_scenario: TestScenario
    """

    def sending_post_listener(self, endpoint: TestAdapterEndpoint, body_dict: Dict):
        immortals_assert(isinstance(endpoint, TestAdapterEndpoint))
        immortals_assert(isinstance(self._expected_activity[0].endpoint, TestAdapterEndpoint))

        validator = self._expected_activity.pop(0)
        # validator = self._expected_activity.pop(0)
        self._pending_acks.append(validator)
        self._log_expected_and_actual(validator=validator, endpoint=endpoint, body_val=body_dict, status_code=None)
        validator.validate_submission(endpoint=endpoint, body_str=json.dumps(body_dict))

        if len(self._expected_activity) == 0 and len(self._pending_acks) == 0:
            self.finished = True

    def received_post_ack_listener(self, endpoint: TestHarnessEndpoint, response_code: int, body_str: str):
        immortals_assert(isinstance(endpoint, TestHarnessEndpoint))
        validator = self._pending_acks.pop(0)
        immortals_assert(isinstance(validator.endpoint, TestHarnessEndpoint))

        # validator = self._expected_activity.pop(0)
        self._log_expected_and_actual(validator=validator, endpoint=endpoint,
                                      body_val=body_str, status_code=response_code)
        validator.validate_ack(endpoint=endpoint, body_str=body_str, status_code=response_code)

        if endpoint == TestHarnessEndpoint.READY:
            submission = self._submissions.pop(0)
            tpr.start_thread(thread_method=submission.execute)

        # elif endpoint == TestHarnessEndpoint.ACTION_DONE:
        #     if len(self._submissions) > 0:
        #         submission = self._submissions.pop(0)
        #         t = Thread(target=submission.execute)
        #         t.setDaemon(True)
        #         t.start()

        if len(self._expected_activity) == 0 and len(self._pending_acks) == 0:
            self.finished = True

    def sent_post_ack_listener(self, endpoint: TestAdapterEndpoint, response_code: int, body_str: str):
        immortals_assert(isinstance(endpoint, TestAdapterEndpoint))
        validator = self._pending_acks.pop(0)
        immortals_assert(isinstance(validator.endpoint, TestAdapterEndpoint))

        self._log_expected_and_actual(validator=validator, endpoint=endpoint,
                                      body_val=body_str, status_code=response_code)
        validator.validate_ack(endpoint=endpoint, body_str=body_str, status_code=response_code)

        if endpoint == TestAdapterEndpoint.ENABLED:
            submission = self._submissions.pop(0)
            tpr.start_thread(thread_method=submission.execute)

        if len(self._expected_activity) == 0 and len(self._pending_acks) == 0:
            self.finished = True

    def receiving_post_listener(self, endpoint: TestHarnessEndpoint, body_str: str):
        immortals_assert(isinstance(endpoint, TestHarnessEndpoint))
        immortals_assert(isinstance(self._expected_activity[0].endpoint, TestHarnessEndpoint))
        validator = self._expected_activity.pop(0)
        self._pending_acks.append(validator)

        # validator = self._expected_activity.pop(0)
        self._log_expected_and_actual(validator=validator, endpoint=endpoint, body_val=body_str, status_code=None)
        validator.validate_submission(endpoint, body_str)

        if len(self._expected_activity) == 0 and len(self._pending_acks) == 0:
            self.finished = True

    def __init__(self, test_scenario: Phase2TestScenario, logger: logging.Logger):

        self.test_scenario = test_scenario
        self._submissions: List[TestAdapterSubmission] = \
            produce_test_adapter_submissions(ll_input=test_scenario.submissionModel,
                                             submission_flow=test_scenario.submissionFlow,
                                             perturbation_scenario=test_scenario.perturbationScenario)
        self.finished: bool = False

        self._logger: logging.Logger = logger
        self._expected_activity: List[NetworkExpectationValidator] = \
            generate_expected_activity(test_scenario=test_scenario)
        self._pending_acks: List[NetworkExpectationValidator] = list()

    def _log_expected_and_actual(self, validator: NetworkExpectationValidator,
                                 endpoint: Union[TestAdapterEndpoint, TestHarnessEndpoint],
                                 body_val: Union[Dict, str, None] = None, status_code: int = None):
        if not keep_going:
            return
        expecting_msg = 'Expecting ' + _get_activity_msg(
            endpoint=validator.endpoint,
            body_val=(validator.submission_values if status_code is None else validator.ack_values),
            status_code=status_code)

        self._logger.info(expecting_msg)

        actual_msg = ''

        if isinstance(endpoint, TestAdapterEndpoint):
            if status_code is None:
                actual_msg += 'Sending   '
            else:
                actual_msg += 'Receiving '

        elif isinstance(endpoint, TestHarnessEndpoint):
            if status_code is None:
                actual_msg += 'Received  '
            else:
                actual_msg += 'Sending   '

        else:
            raise Exception('Unexpected endpoint type' + endpoint.__class__.__name__)

        actual_msg += _get_activity_msg(
            endpoint=endpoint,
            body_val=body_val,
            status_code=status_code
        )

        if tpr.keep_running():
            self._logger.info(actual_msg)


class TABehaviorValidator(Phase2TestHarnessListenerInterface):
    """
    :type _scenario_executions: list[ScenarioExecution]
    """

    def __init__(self, done_listener: Callable, test_suite_identifier: str):
        self._log_filename: str = test_suite_identifier + '-results.txt'
        lfh = logging.FileHandler(self._log_filename)
        lfh.setFormatter(logging.Formatter('%(message)s'))
        self._logger: logging.Logger = logging.getLogger(self._log_filename)
        self._logger.setLevel(logging.INFO)
        self._logger.addHandler(lfh)
        sh = logging.StreamHandler(stream=sys.stdout)
        self._logger.addHandler(sh)
        self._lock: RLock = RLock()

        self._done_listener: Callable = done_listener

        test_scenarios_d = resourcemanager.get_test_suite(test_suite_identifier)

        self._scenario_executions: List[ScenarioExecution] = []

        for test in test_scenarios_d:
            self._scenario_executions.append(
                ScenarioExecution(Phase2TestScenario.from_dict(test), self._logger))

    def get_current_test_scenario(self) -> Optional[Phase2TestScenario]:
        if len(self._scenario_executions) > 0:
            return self._scenario_executions[0].test_scenario
        else:
            return None

    def sent_post_ack_listener(self, endpoint: TestAdapterEndpoint, response_code: int, body_str: str):
        with self._lock:
            self._scenario_executions[0].sent_post_ack_listener(endpoint=endpoint,
                                                                response_code=response_code,
                                                                body_str=body_str)

            if self._scenario_executions[0].finished:
                self._scenario_executions.pop(0)
                self._done_listener(self.get_current_test_scenario())

    def received_post_ack_listener(self, endpoint: TestHarnessEndpoint, response_code: int, body_str: str):
        with self._lock:
            self._scenario_executions[0].received_post_ack_listener(
                endpoint=endpoint, response_code=response_code, body_str=body_str)

            if self._scenario_executions[0].finished:
                self._scenario_executions.pop(0)
                self._done_listener(self.get_current_test_scenario())

    def receiving_post_listener(self, endpoint: TestHarnessEndpoint, body_str: str):
        with self._lock:
            body_dict = self.parse_posted_data(endpoint=endpoint, body_str=body_str)
            self._scenario_executions[0].receiving_post_listener(
                endpoint=endpoint,
                body_str=(None if body_dict is None else json.dumps(body_dict)))
            # self._scenario_executions[0].receive_post(endpoint=endpoint, body_str=json.dumps(body_dict))

            if self._scenario_executions[0].finished:
                self._scenario_executions.pop(0)
                self._done_listener(self.get_current_test_scenario())

    def sending_post_listener(self, endpoint: TestAdapterEndpoint, body_dict: Dict):
        with self._lock:
            self._scenario_executions[0].sending_post_listener(endpoint=endpoint, body_dict=body_dict)

            if self._scenario_executions[0].finished:
                self._scenario_executions.pop(0)
                self._done_listener(self.get_current_test_scenario())

    def parse_posted_data(self, endpoint: TestHarnessEndpoint, body_str: str) -> Union[Dict, str, None]:
        if endpoint.submitDatatype is None:
            # TODO: Should I pay attention to body data I don't care about?
            return None

        elif endpoint.submitDatatype == str:
            return body_str

        elif issubclass(endpoint.submitDatatype, Serializable):
            return endpoint.submitDatatype.from_json_str(body_str, value_pool=None, do_replacement=False).to_dict()

        else:
            raise Exception('Unaccounted data type "' + str(endpoint.submitDatatype) + '" Expected for endpoint!')
