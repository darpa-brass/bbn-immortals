from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.measurement.codeunitpointer import CodeUnitPointer
from pymmortals.generated.com.securboration.immortals.ontology.measurement.measurementinstance import MeasurementInstance
from typing import List


# noinspection PyPep8Naming
class MeasurementProfile(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 codeUnit: CodeUnitPointer = None,
                 measurement: List[MeasurementInstance] = None):
        super().__init__()
        self.codeUnit = codeUnit
        self.measurement = measurement
