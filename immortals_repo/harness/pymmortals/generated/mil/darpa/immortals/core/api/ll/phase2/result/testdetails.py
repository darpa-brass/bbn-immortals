from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.testoutcome import TestOutcome
from typing import List


# noinspection PyPep8Naming
class TestDetails(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 currentState: TestOutcome = None,
                 detailMessages: List[str] = None,
                 errorMessages: List[str] = None,
                 testIdentifier: str = None):
        super().__init__()
        self.currentState = currentState
        self.detailMessages = detailMessages
        self.errorMessages = errorMessages
        self.testIdentifier = testIdentifier
