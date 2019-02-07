from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.verdictoutcome import VerdictOutcome
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.teststateobject import TestStateObject
from typing import List


# noinspection PyPep8Naming
class ValidationStateObject(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 executedTests: List[TestStateObject] = None,
                 testsPassedPercent: float = None,
                 verdictOutcome: VerdictOutcome = None):
        super().__init__()
        self.executedTests = executedTests
        self.testsPassedPercent = testsPassedPercent
        self.verdictOutcome = verdictOutcome
