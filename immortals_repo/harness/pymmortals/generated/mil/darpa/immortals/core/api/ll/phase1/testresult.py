from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.status import Status
from typing import List


# noinspection PyPep8Naming
class TestResult(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 currentState: Status = None,
                 detailMessages: List[str] = None,
                 errorMessages: List[str] = None,
                 validatorIdentifier: str = None):
        super().__init__()
        self.currentState = currentState
        self.detailMessages = detailMessages
        self.errorMessages = errorMessages
        self.validatorIdentifier = validatorIdentifier
