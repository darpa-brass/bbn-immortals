from pymmortals.datatypes.serializable import Serializable
from typing import List


# noinspection PyPep8Naming
class ValidationStartReturnData(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 expectedDurationSeconds: int = None,
                 validatorIdentifiers: List[str] = None):
        super().__init__()
        self.expectedDurationSeconds = expectedDurationSeconds
        self.validatorIdentifiers = validatorIdentifiers
