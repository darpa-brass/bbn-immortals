import json
import logging
import sys
from threading import RLock, Thread
from typing import Dict, Union, List, Callable

from pymmortals.datatypes.deployment_model import LLP1Input
from pymmortals.datatypes.serializable import Serializable
from pymmortals.datatypes.testing_p1 import AbstractHarnessListener, SubmissionFlow, TestScenario, \
    SubmissionResult, Endpoint
from pymmortals.resources import resourcemanager
from pymmortals.testing.phase1.ta_submitter import produce_test_adapter_submissions, TestAdapterSubmission


def prettify(body: Union[Serializable, Dict[str, str]] or None) -> Union[str, None]:
    return_body = None

    if body is None:
        return ''

    if isinstance(body, Serializable):
        return body.to_json_str_pretty()

    elif isinstance(body, dict):
        return json.dumps(body, indent=4, separators=(',', ': '))

    elif isinstance(body, str):
        return json.dumps(json.loads(body), indent=4, separators=(',', ': '))

    return return_body


class LoggingListener(AbstractHarnessListener):
    def receiving_post_listener(self, endpoint: Endpoint, body_str: str):
        body_dict = endpoint.parse_from_ta(body_str=body_str)
        msg = ('TH RECEIVED POST ' + endpoint.path + ' with BODY:\n' + prettify(body=body_dict) + '\n')
        self.network_logger.info(msg)

    def sent_post_ack_listener(self, endpoint: Endpoint, response_code: int, body_str: str):
        body_dict = endpoint.parse_ack_to_ta(body_str=body_str)
        msg = ('TH RECEIVED ACK for POST ' + endpoint.path + ' of ' + str(response_code)
               + ' with BODY:\n' + prettify(body=body_dict) + '\n')
        self.network_logger.info(msg)

    def received_post_ack_listener(self, endpoint: Endpoint, response_code: int, body_str: str):
        body_dict = endpoint.parse_from_ta(body_str=body_str)
        msg = ('TH RESPONDING TO POST ' + endpoint.path + ' with CODE=' + str(response_code)
               + ' and BODY:\n' + prettify(body=body_dict) + '\n')
        self.network_logger.info(msg)

    def sending_post_listener(self, endpoint: Endpoint, body_dict: Dict):
        msg = ('TH SENDING POST ' + endpoint.path + ' with BODY:\n' + prettify(body=body_dict) + '\n')
        self.network_logger.info(msg)

    def __init__(self,
                 log_filepath: str):
        lfh = logging.FileHandler(log_filepath)
        lfh.setFormatter(logging.Formatter('%(message)s'))
        self.network_logger = logging.getLogger(log_filepath)
        self.network_logger.setLevel(logging.INFO)
        self.network_logger.addHandler(lfh)
        sh = logging.StreamHandler(stream=sys.stdout)
        self.network_logger.addHandler(sh)


class DefaultExecutionListener(LoggingListener):
    def __init__(self, log_filepath: str, submission_flow: SubmissionFlow, ll_input: LLP1Input):
        LoggingListener.__init__(self, log_filepath=log_filepath)

        self._pending_submissions: List[TestAdapterSubmission] = \
            produce_test_adapter_submissions(submission_flow, ll_input)

    def sent_post_ack_listener(self, endpoint: Endpoint, response_code: int, body_str: str):
        LoggingListener.sent_post_ack_listener(self, endpoint, response_code, body_str)

    def received_post_ack_listener(self, endpoint: Endpoint, response_code: int, body_str: str):
        LoggingListener.received_post_ack_listener(self, endpoint, response_code, body_str)

    def receiving_post_listener(self, endpoint: Endpoint, body_str: str):
        LoggingListener.receiving_post_listener(self, endpoint, body_str)

        if endpoint == Endpoint.READY:
            self._pending_submissions[0].execute()
            self._pending_submissions.pop(0)

        if endpoint == Endpoint.ACTION_DONE:
            if len(self._pending_submissions) > 0:
                self._pending_submissions[0].execute()
                self._pending_submissions.pop(0)

    def sending_post_listener(self, endpoint: Endpoint, body_dict: Dict):
        LoggingListener.sending_post_listener(self, endpoint, body_dict)


class NetworkResponseValidator:
    def __init__(self, expected_endpoint: Endpoint, expected_received_values: Dict[str, str] = None,
                 expected_status_code: int = None):
        self.expected_endpoint: Endpoint = expected_endpoint
        self.expected_received_values = expected_received_values
        self.expected_status_code = expected_status_code

    def receive_and_validate(self, endpoint: Endpoint, body_str: str, status_code: int = None):
        assert endpoint == self.expected_endpoint, \
            ('Endpoint ' + endpoint.name + ' != ' + str(self.expected_endpoint.name))

        if self.expected_status_code is not None:
            assert self.expected_status_code == status_code, \
                ('Status code ' + str(status_code) + ' != ' + str(self.expected_status_code))

        if self.expected_received_values is not None:
            for key in self.expected_received_values:
                target_d = json.loads(body_str)

                override_path = key.split('.')
                override_tail = override_path.pop()

                for path_element in override_path:
                    value = target_d[path_element]
                    target_d = value

                assert target_d[override_tail] == self.expected_received_values[key], \
                    ('Value of "' + str(target_d[override_tail]) + '" for "' + key
                     + '" Does not match expected value of "' + self.expected_received_values[key] + '"!')


def _get_adaptation_started_response_validators() -> List[NetworkResponseValidator]:
    return [
        NetworkResponseValidator(
            expected_endpoint=Endpoint.STATUS,
            expected_received_values={
                'STATUS': 'PERTURBATION_DETECTED'}),
        NetworkResponseValidator(
            expected_endpoint=Endpoint.STATUS,
            expected_received_values={
                'STATUS': 'ADAPTING'})
    ]


def _get_adaptation_response_no_solution_validators() -> List[NetworkResponseValidator]:
    return_value = _get_adaptation_started_response_validators()
    return_value.append(
        NetworkResponseValidator(
            expected_endpoint=Endpoint.STATUS,
            expected_received_values={
                'STATUS': 'MISSION_ABORTED',
                'MESSAGE.adaptation.adaptationStatus': 'FAILURE',
                'MESSAGE.adaptation.details.adaptationStatusValue': 'UNSUCCESSFUL',
                'MESSAGE.validation.overallIntentStatus': 'PENDING'
            })
    )
    return return_value


def _get_adaptation_response_successful_validators() -> List[NetworkResponseValidator]:
    return _get_adaptation_started_response_validators() + [
        NetworkResponseValidator(
            expected_endpoint=Endpoint.STATUS,
            expected_received_values={
                'STATUS': 'ADAPTATION_COMPLETED',
                'MESSAGE.adaptation.adaptationStatus': 'SUCCESS',
                'MESSAGE.adaptation.details.adaptationStatusValue': 'SUCCESSFUL',
                'MESSAGE.validation.overallIntentStatus': 'PENDING'}),
        NetworkResponseValidator(
            expected_endpoint=Endpoint.ACTION_DONE,
            expected_received_values={
                'ARGUMENTS.adaptation.adaptationStatus': 'SUCCESS',
                'ARGUMENTS.adaptation.details.adaptationStatusValue': 'SUCCESSFUL',
                'ARGUMENTS.validation.overallIntentStatus': 'SUCCESS'
            }
        )
    ]


class ScenarioExecution:
    """
    :type finished: bool
    :type test_scenario: TestScenario
    """

    def __init__(self, test_scenario: TestScenario, logger: logging.Logger):

        self.test_scenario = test_scenario
        self._submissions: List[TestAdapterSubmission] = \
            produce_test_adapter_submissions(test_scenario.submissionFlow, test_scenario.deploymentModel)
        self.finished: bool = False

        self._logger: logging.Logger = logger

        if test_scenario.submissionFlow == SubmissionFlow.CHALLENGE:

            if test_scenario.expectedResult == SubmissionResult.INVALID_SUBMISSION:
                self._received_posts: List[NetworkResponseValidator] = [
                    NetworkResponseValidator(
                        expected_endpoint=Endpoint.READY),
                    NetworkResponseValidator(
                        expected_endpoint=Endpoint.ERROR)
                ]
                self._submission_responses: List[NetworkResponseValidator] = [
                    NetworkResponseValidator(
                        expected_endpoint=Endpoint.ACTION_ADAPT_AND_VALIDATE,
                        expected_status_code=400)
                ]

            elif test_scenario.expectedResult == SubmissionResult.NO_SOLUTION:
                self._received_posts: List[NetworkResponseValidator] = \
                    [NetworkResponseValidator(expected_endpoint=Endpoint.READY)]
                self._received_posts += _get_adaptation_response_no_solution_validators()

                self._submission_responses: List[NetworkResponseValidator] = [
                    NetworkResponseValidator(
                        expected_endpoint=Endpoint.ACTION_ADAPT_AND_VALIDATE,
                        expected_status_code=200)
                ]

            elif test_scenario.expectedResult == SubmissionResult.VALID:
                self._received_posts: List[NetworkResponseValidator] = [
                    NetworkResponseValidator(
                        expected_endpoint=Endpoint.READY)
                ]
                self._received_posts += _get_adaptation_response_successful_validators()
                self._submission_responses: List[NetworkResponseValidator] = [
                    NetworkResponseValidator(
                        expected_endpoint=Endpoint.ACTION_ADAPT_AND_VALIDATE,
                        expected_status_code=200)
                ]

            else:
                raise Exception(
                    'Unexpected scenario result expectation of "' + test_scenario.expectedResult.identifier + '"!')

        elif test_scenario.submissionFlow == SubmissionFlow.ALL:
            self._received_posts: List[NetworkResponseValidator] = [
                NetworkResponseValidator(expected_endpoint=Endpoint.READY),

                NetworkResponseValidator(
                    expected_endpoint=Endpoint.ACTION_DONE,
                    expected_received_values={
                        'ARGUMENTS.adaptation.adaptationStatus': 'NOT_APPLICABLE',
                        'ARGUMENTS.validation.overallIntentStatus': 'SUCCESS'}),
                NetworkResponseValidator(
                    expected_endpoint=Endpoint.ACTION_DONE,
                    expected_received_values={
                        'ARGUMENTS.adaptation.adaptationStatus': 'NOT_APPLICABLE',
                        'ARGUMENTS.validation.overallIntentStatus': 'FAILURE'})
            ]

            self._received_posts += _get_adaptation_response_successful_validators()

            self._submission_responses: List[NetworkResponseValidator] = [
                NetworkResponseValidator(
                    expected_endpoint=Endpoint.ACTION_VALIDATE_BASELINE,
                    expected_status_code=200,
                    expected_received_values={
                        'RESULT.adaptation.adaptationStatus': 'NOT_APPLICABLE',
                        'RESULT.validation.overallIntentStatus': 'PENDING'}),
                NetworkResponseValidator(
                    expected_endpoint=Endpoint.ACTION_VALIDATE_BASELINE,
                    expected_status_code=200,
                    expected_received_values={
                        'RESULT.adaptation.adaptationStatus': 'NOT_APPLICABLE',
                        'RESULT.validation.overallIntentStatus': 'PENDING'}),
                NetworkResponseValidator(
                    expected_endpoint=Endpoint.ACTION_ADAPT_AND_VALIDATE,
                    expected_status_code=200,
                    expected_received_values={
                        'RESULT.adaptation.adaptationStatus': 'PENDING',
                        'RESULT.validation.overallIntentStatus': 'PENDING'})
            ]

        elif test_scenario.submissionFlow == SubmissionFlow.BASELINE:
            self._received_posts: List[NetworkResponseValidator] = [
                NetworkResponseValidator(expected_endpoint=Endpoint.READY),
                NetworkResponseValidator(
                    expected_endpoint=Endpoint.ACTION_DONE,
                    expected_received_values={
                        'ARGUMENTS.adaptation.adaptationStatus': 'NOT_APPLICABLE',
                        'ARGUMENTS.validation.overallIntentStatus': 'SUCCESS'
                    })
            ]

            self._submission_responses: List[NetworkResponseValidator] = [
                NetworkResponseValidator(
                    expected_endpoint=Endpoint.ACTION_VALIDATE_BASELINE,
                    expected_status_code=200,
                    expected_received_values={
                        'RESULT.adaptation.adaptationStatus': 'NOT_APPLICABLE',
                        'RESULT.validation.overallIntentStatus': 'PENDING'})
            ]

        else:
            raise Exception(
                'Unsupported submission flow of "' + test_scenario.submissionFlow.identifier + '" provided!')

    def _log_expected_and_actual(self, validator: NetworkResponseValidator, endpoint: Endpoint,
                                 body_str: str = None, status_code: int = None):
        self._logger.info(
            'Expecting '
            + ('' if validator.expected_status_code is None else (str(validator.expected_status_code) + ' from '))
            + str(validator.expected_endpoint.name) +
            ('' if validator.expected_received_values is None else
             (' with BODY PARAMETERS: ' + prettify(validator.expected_received_values))))

        self._logger.info('Received '
                          + ('' if status_code is None else (str(status_code) + ' from '))
                          + endpoint.name
                          + ('' if (body_str is None or body_str == '') else (' with BODY: ' + prettify(body_str))))

    def receive_post(self, endpoint: Endpoint, body_str=None):
        validator = self._received_posts.pop(0)
        self._log_expected_and_actual(validator=validator, endpoint=endpoint, body_str=body_str, status_code=None)
        validator.receive_and_validate(endpoint=endpoint, body_str=body_str, status_code=None)

        if endpoint == Endpoint.READY:
            submission = self._submissions.pop(0)
            t = Thread(target=submission.execute)
            t.setDaemon(True)
            t.start()

        elif endpoint == Endpoint.ACTION_DONE:
            if len(self._submissions) > 0:
                submission = self._submissions.pop(0)
                t = Thread(target=submission.execute)
                t.setDaemon(True)
                t.start()

        if len(self._received_posts) == 0 and len(self._submission_responses) == 0:
            self.finished = True

    def receive_post_response(self, endpoint, body_str=None, status_code=None):
        validator = self._submission_responses.pop(0)
        self._log_expected_and_actual(validator=validator, endpoint=endpoint,
                                      body_str=body_str, status_code=status_code)
        validator.receive_and_validate(endpoint=endpoint, body_str=body_str, status_code=status_code)

        if len(self._received_posts) == 0 and len(self._submission_responses) == 0:
            self.finished = True


class TABehaviorValidator(AbstractHarnessListener):
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
                ScenarioExecution(TestScenario.from_dict(test), self._logger))

    def get_current_test_scenario(self) -> Union[TestScenario, None]:
        if len(self._scenario_executions) > 0:
            return self._scenario_executions[0].test_scenario
        else:
            return None

    def sent_post_ack_listener(self, endpoint: Endpoint, response_code: int, body_str: str):
        with self._lock:
            self._scenario_executions[0].receive_post_response(endpoint=endpoint,
                                                               body_str=body_str,
                                                               status_code=response_code)

            if self._scenario_executions[0].finished:
                execution = self._scenario_executions.pop(0)
                self._done_listener(execution.test_scenario.scenarioIdentifier,
                                    execution.test_scenario.deploymentModel.sessionIdentifier,
                                    self.get_current_test_scenario())

    def received_post_ack_listener(self, endpoint: Endpoint, response_code: int, body_str: str):
        pass

    def receiving_post_listener(self, endpoint: Endpoint, body_str: str):
        with self._lock:
            body_dict = endpoint.parse_from_ta(body_str=body_str)
            self._scenario_executions[0].receive_post(endpoint=endpoint, body_str=json.dumps(body_dict))

            if self._scenario_executions[0].finished:
                execution = self._scenario_executions.pop(0)
                self._done_listener(execution.test_scenario.scenarioIdentifier,
                                    execution.test_scenario.deploymentModel.sessionIdentifier,
                                    self.get_current_test_scenario())

    def sending_post_listener(self, endpoint: Endpoint, body_dict: Dict):
        pass
