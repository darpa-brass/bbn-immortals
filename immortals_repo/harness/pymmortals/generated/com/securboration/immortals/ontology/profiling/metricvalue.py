from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.profiling.metrictype import MetricType
from pymmortals.generated.com.securboration.immortals.ontology.profiling.value import Value


# noinspection PyPep8Naming
class MetricValue(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 metric: MetricType = None,
                 value: Value = None):
        super().__init__()
        self.metric = metric
        self.value = value
