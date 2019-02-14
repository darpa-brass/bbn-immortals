from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.analysis.dataflowedge import DataflowEdge
from typing import List


# noinspection PyPep8Naming
class DataflowGraph(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 edges: List[DataflowEdge] = None,
                 humanReadableDescription: str = None):
        super().__init__()
        self.edges = edges
        self.humanReadableDescription = humanReadableDescription
