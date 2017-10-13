from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.profiling.performanceresult import PerformanceResult
from typing import List


# noinspection PyPep8Naming
class PerformanceResults(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 results: List[PerformanceResult] = None):
        super().__init__()
        self.results = results
