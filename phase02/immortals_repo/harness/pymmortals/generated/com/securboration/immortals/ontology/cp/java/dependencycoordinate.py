from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.bytecodeartifact import BytecodeArtifact
from typing import List


# noinspection PyPep8Naming
class DependencyCoordinate(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 artifacts: List[BytecodeArtifact] = None):
        super().__init__()
        self.artifacts = artifacts
