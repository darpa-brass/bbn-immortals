from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.analysis.profiling.simpleresourcedependencyassertion import SimpleResourceDependencyAssertion
from pymmortals.generated.com.securboration.immortals.ontology.measurement.measurementprofile import MeasurementProfile
from typing import List


# noinspection PyPep8Naming
class AnalysisReport(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 discoveredDependency: List[SimpleResourceDependencyAssertion] = None,
                 measurementProfile: List[MeasurementProfile] = None):
        super().__init__()
        self.discoveredDependency = discoveredDependency
        self.measurementProfile = measurementProfile
