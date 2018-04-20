from pymmortals.datatypes.serializable import Serializable
from typing import Set


# noinspection PyPep8Naming
class TestCaseReport(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 duration: float = None,
                 failureMessage: str = None,
                 testCaseIdentifier: str = None,
                 testCaseTarget: str = None,
                 validatedFunctionality: Set[str] = None):
        super().__init__()
        self.duration = duration
        self.failureMessage = failureMessage
        self.testCaseIdentifier = testCaseIdentifier
        self.testCaseTarget = testCaseTarget
        self.validatedFunctionality = validatedFunctionality
