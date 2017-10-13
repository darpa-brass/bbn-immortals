from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import Type


# noinspection PyPep8Naming
class MeasurementType(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 correspondingProperty: Type[Property] = None,
                 measurementType: str = None):
        super().__init__()
        self.correspondingProperty = correspondingProperty
        self.measurementType = measurementType
