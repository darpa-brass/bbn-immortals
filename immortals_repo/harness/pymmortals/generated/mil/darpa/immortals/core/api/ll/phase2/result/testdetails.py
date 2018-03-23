from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.abstracttestdetails import AbstractTestDetails
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.testoutcome import TestOutcome
from typing import List


# noinspection PyPep8Naming
class TestDetails(AbstractTestDetails):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 adaptationIdentifier: str = None,
                 currentState: TestOutcome = None,
                 detailMessages: List[str] = None,
                 errorMessages: List[str] = None,
                 testIdentifier: str = None,
                 timestamp: int = None):
        super().__init__(adaptationIdentifier=adaptationIdentifier, currentState=currentState, testIdentifier=testIdentifier, timestamp=timestamp)
        self.detailMessages = detailMessages
        self.errorMessages = errorMessages
