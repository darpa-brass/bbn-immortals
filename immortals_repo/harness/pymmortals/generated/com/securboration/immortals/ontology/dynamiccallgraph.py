from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.dynamiccallgraphedge import DynamicCallGraphEdge
from typing import List


# noinspection PyPep8Naming
class DynamicCallGraph(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 dynamicCallGraphEdges: List[DynamicCallGraphEdge] = None):
        super().__init__()
        self.dynamicCallGraphEdges = dynamicCallGraphEdges
