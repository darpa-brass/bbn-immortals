from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.status import Status
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testdetails import TestDetails
from typing import List


# noinspection PyPep8Naming
class ValidationState(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 executedTests: List[TestDetails] = None,
                 overallIntentStatus: Status = None):
        super().__init__()
        self.executedTests = executedTests
        self.overallIntentStatus = overallIntentStatus
