from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.measurement.metricprofile import MetricProfile
from typing import List


# noinspection PyPep8Naming
class MetricSet(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 profiles: List[MetricProfile] = None):
        super().__init__()
        self.profiles = profiles
