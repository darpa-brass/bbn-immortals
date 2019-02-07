from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase1.testresult import TestResult
from typing import List


# noinspection PyPep8Naming
class ValidationResults(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 results: List[TestResult] = None,
                 testDurationMS: int = None):
        super().__init__()
        self.results = results
        self.testDurationMS = testDurationMS
