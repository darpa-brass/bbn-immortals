from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.measurement.codeunitpointer import CodeUnitPointer
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List


# noinspection PyPep8Naming
class MetricProfile(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 codeUnit: CodeUnitPointer = None,
                 humanReadableDesc: str = None,
                 measuredProperty: List[Property] = None):
        super().__init__()
        self.codeUnit = codeUnit
        self.humanReadableDesc = humanReadableDesc
        self.measuredProperty = measuredProperty
