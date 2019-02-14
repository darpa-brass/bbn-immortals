from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.testoutcome import TestOutcome
from typing import Set


# noinspection PyPep8Naming
class AbstractTestDetails(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 adaptationIdentifier: str = None,
                 currentState: TestOutcome = None,
                 testIdentifier: str = None,
                 timestamp: int = None,
                 validatedFunctionality: Set[str] = None):
        super().__init__()
        self.adaptationIdentifier = adaptationIdentifier
        self.currentState = currentState
        self.testIdentifier = testIdentifier
        self.timestamp = timestamp
        self.validatedFunctionality = validatedFunctionality
