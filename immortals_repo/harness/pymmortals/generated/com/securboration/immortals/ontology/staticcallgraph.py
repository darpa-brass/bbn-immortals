from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.staticcallgraphedge import StaticCallGraphEdge
from typing import List


# noinspection PyPep8Naming
class StaticCallGraph(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 staticCallGraphEdges: List[StaticCallGraphEdge] = None):
        super().__init__()
        self.staticCallGraphEdges = staticCallGraphEdges
