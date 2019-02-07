from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.abstracttestdetails import AbstractTestDetails
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.testoutcome import TestOutcome
from integrationtest.generated.mil.darpa.immortals.core.api.testcasereport import TestCaseReport
from typing import List
from typing import Set


# noinspection PyPep8Naming
class TestDetails(AbstractTestDetails):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 adaptationIdentifier: str = None,
                 currentState: TestOutcome = None,
                 detailMessages: List[str] = None,
                 errorMessages: List[str] = None,
                 testCaseReport: TestCaseReport = None,
                 testIdentifier: str = None,
                 timestamp: int = None,
                 validatedFunctionality: Set[str] = None):
        super().__init__(adaptationIdentifier=adaptationIdentifier, currentState=currentState, testIdentifier=testIdentifier, timestamp=timestamp, validatedFunctionality=validatedFunctionality)
        self.detailMessages = detailMessages
        self.errorMessages = errorMessages
        self.testCaseReport = testCaseReport
