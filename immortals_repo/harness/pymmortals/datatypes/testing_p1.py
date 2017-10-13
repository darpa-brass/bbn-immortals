import json
from typing import Dict, Union, FrozenSet

from pymmortals.datatypes.deployment_model import LLP1Input
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.lldaserror import LLDasError
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.lldasstatus import LLDasStatus
from .serializable import Serializable, ValuedEnum


class SubmissionFlow(ValuedEnum):
    BASELINE = 'baseline'
    CHALLENGE = 'challenge'
    ALL = 'all'

    @property
    def identifier(self) -> str:
        return self._value_

    @classmethod
    def all_identifiers(cls) -> FrozenSet[str]:
        return cls._values()


class SubmissionResult(ValuedEnum):
    INVALID_SUBMISSION = 'invalidSubmission'
    NO_SOLUTION = 'noSolution'
    VALID = 'valid'

    @property
    def identifier(self) -> str:
        return self._value_

    @classmethod
    def all_identifiers(cls) -> FrozenSet[str]:
        return cls._values()


class Endpoint(ValuedEnum):
    ACTION_DONE = '/action/done'
    ERROR = '/error'
    STATUS = '/status'
    READY = '/ready'
    ACTION_VALIDATE_BASELINE = '/action/validateBaselineApplication'
    ACTION_ADAPT_AND_VALIDATE = '/action/adaptAndValidateApplication'

    @property
    def path(self) -> str:
        return self._value_

    @classmethod
    def all_paths(cls) -> FrozenSet[str]:
        return cls._values()

    def parse_ack_to_ta(self, body_str: Union[str, bytes]) -> Dict[str, object]:
        if isinstance(body_str, bytes):
            body_str = body_str.decode()

        body = json.loads(body_str)  # type: Dict[str, object]
        field = None

        try:

            if self == Endpoint.ERROR or self == Endpoint.STATUS or self == Endpoint.READY:
                pass

            elif self == Endpoint.ACTION_VALIDATE_BASELINE or self == Endpoint.ACTION_ADAPT_AND_VALIDATE \
                    or self == Endpoint.ACTION_DONE:
                field = 'TIME'
                assert (field in body)
                field = 'RESULT'
                assert (field in body)
                field = 'identifier'
                assert (field in body['RESULT'])
                field = 'adaptation'
                assert (field in body['RESULT'])
                field = 'validation'
                assert (field in body['RESULT'])

            else:
                raise Exception('Unknown endpoint "' + str(self) + '"!')

        except AssertionError as ae:
            if field is not None:
                raise Exception('Missing field "' + field + '" for endpoint "' + self.name + '" in JSON: "' +
                                json.dumps(body, indent=4, separators=(',', ': ')))

            else:
                raise ae

        return body

    def parse_from_ta(self, body_str: Union[str, bytes]) -> Dict[str, str]:
        if isinstance(body_str, bytes):
            body_str = body_str.decode()

        body: Dict[str, str] = json.loads(body_str)
        field = None
        status = None
        error = None

        try:
            field = None
            status = None
            error = None
            if self == Endpoint.ERROR:
                body: Dict[str, str] = json.loads(body)
                field = 'TIME'
                assert (field in body)
                field = 'ERROR'
                assert (field in body)
                error = body['ERROR']
                assert (error in LLDasError.all_tag())
                if 'MESSAGE' in body and body['MESSAGE'].startswith('{'):
                    body['MESSAGE'] = json.loads(body['MESSAGE'])

            elif self == Endpoint.STATUS:
                field = 'TIME'
                assert (field in body)
                field = 'STATUS'
                assert (field in body)
                status = body['STATUS']
                assert (status in LLDasStatus.all_tag())
                if 'MESSAGE' in body and body['MESSAGE'].startswith('{'):
                    body['MESSAGE'] = json.loads(body['MESSAGE'])

            elif self == Endpoint.READY:
                field = 'TIME'
                assert (field in body)

            elif self == Endpoint.ACTION_VALIDATE_BASELINE or self == Endpoint.ACTION_ADAPT_AND_VALIDATE \
                    or self == Endpoint.ACTION_DONE:
                field = 'TIME'
                assert (field in body)
                # field = 'TARGET'
                # assert (field in body)
                field = 'ARGUMENTS'
                assert (field in body)
                field = 'identifier'
                assert (field in body['ARGUMENTS'])
                field = 'adaptation'
                assert (field in body['ARGUMENTS'])
                field = 'validation'
                assert (field in body['ARGUMENTS'])

            else:
                raise Exception('Unknown endpoint "' + str(self) + '"!')

        except AssertionError as ae:
            if field is not None:
                raise Exception('Missing field "' + field + '" for endpoint "' + self.name + '" in JSON: "' +
                                json.dumps(body, indent=4, separators=(',', ': ')))

            elif status is not None:
                raise Exception('Status "' + status + '" is not a valid STATUS in JSON: ' +
                                json.dumps(body, indent=4, separators=(',', ': ')))

            elif error is not None:
                raise Exception('Error "' + error + '" is not a valid ERROR in JSON: ' +
                                json.dumps(body, indent=4, separators=(',', ': ')))

            else:
                raise ae

        return body


# noinspection PyPep8Naming
class TestScenario(Serializable):
    _validator_values = {}

    def __init__(self,
                 submissionFlow: SubmissionFlow,
                 expectedResult: SubmissionResult,
                 scenarioIdentifier: str,
                 deploymentModel: LLP1Input,
                 rootConfigurationModifications: Dict[str, str]):
        super().__init__()
        self.submissionFlow = submissionFlow
        self.expectedResult = expectedResult
        self.scenarioIdentifier = scenarioIdentifier
        self.deploymentModel = deploymentModel
        self.rootConfigurationModifications = rootConfigurationModifications


# noinspection PyClassHasNoInit
class AbstractHarnessListener:
    def receiving_post_listener(self, endpoint: Endpoint, body_str: str):
        raise NotImplementedError

    def sending_post_listener(self, endpoint: Endpoint, body_dict: Dict):
        raise NotImplementedError

    def received_post_ack_listener(self, endpoint: Endpoint, response_code: int, body_str: str):
        raise NotImplementedError

    def sent_post_ack_listener(self, endpoint: Endpoint, response_code: int, body_str: str):
        raise NotImplementedError
