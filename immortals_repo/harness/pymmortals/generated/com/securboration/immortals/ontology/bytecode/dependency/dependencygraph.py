from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.dependency.librarydependencyedge import LibraryDependencyEdge
from typing import List


# noinspection PyPep8Naming
class DependencyGraph(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 dependencyEdge: List[LibraryDependencyEdge] = None):
        super().__init__()
        self.dependencyEdge = dependencyEdge
