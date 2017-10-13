from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.measurement.codeunitpointer import CodeUnitPointer


# noinspection PyPep8Naming
class CallGraphEdge(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 calledMethod: CodeUnitPointer = None,
                 originMethod: CodeUnitPointer = None):
        super().__init__()
        self.calledMethod = calledMethod
        self.originMethod = originMethod
