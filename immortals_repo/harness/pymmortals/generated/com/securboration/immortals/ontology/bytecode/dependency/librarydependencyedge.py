from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.dependency import Dependency
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.jarartifact import JarArtifact
from typing import List


# noinspection PyPep8Naming
class LibraryDependencyEdge(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 artifact: JarArtifact = None,
                 dependency: List[Dependency] = None):
        super().__init__()
        self.artifact = artifact
        self.dependency = dependency
