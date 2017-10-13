from pymmortals.datatypes.serializable import Serializable
from typing import List


# noinspection PyPep8Naming
class ValidationStartData(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 clientIdentifiers: List[str] = None,
                 maxRuntimeMS: int = None,
                 minRuntimeMS: int = None,
                 sessionIdentifier: str = None,
                 validatorIdentifiers: List[str] = None):
        super().__init__()
        self.clientIdentifiers = clientIdentifiers
        self.maxRuntimeMS = maxRuntimeMS
        self.minRuntimeMS = minRuntimeMS
        self.sessionIdentifier = sessionIdentifier
        self.validatorIdentifiers = validatorIdentifiers
