from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.profiling.metricvalue import MetricValue
from pymmortals.generated.com.securboration.immortals.ontology.profiling.testplatforminfo import TestPlatformInfo
from typing import List


# noinspection PyPep8Naming
class PerformanceResult(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 metricValues: List[MetricValue] = None,
                 testPlatform: TestPlatformInfo = None):
        super().__init__()
        self.metricValues = metricValues
        self.testPlatform = testPlatform
