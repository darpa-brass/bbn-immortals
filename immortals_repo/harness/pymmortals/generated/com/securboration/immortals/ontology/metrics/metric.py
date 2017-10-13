from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.metrics.measurementtype import MeasurementType
from typing import Type


# noinspection PyPep8Naming
class Metric(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 applicableResourceInstance: Resource = None,
                 applicableResourceType: Type[Resource] = None,
                 linkId: str = None,
                 measurementType: MeasurementType = None,
                 unit: str = None,
                 value: str = None):
        super().__init__()
        self.applicableResourceInstance = applicableResourceInstance
        self.applicableResourceType = applicableResourceType
        self.linkId = linkId
        self.measurementType = measurementType
        self.unit = unit
        self.value = value
