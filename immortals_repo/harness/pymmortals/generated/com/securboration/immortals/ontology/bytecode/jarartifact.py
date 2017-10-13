from pymmortals.generated.com.securboration.immortals.ontology.bytecode.bytecodeartifact import BytecodeArtifact
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.bytecodeartifactcoordinate import BytecodeArtifactCoordinate
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.classpathelement import ClasspathElement
from typing import List


# noinspection PyPep8Naming
class JarArtifact(BytecodeArtifact):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 binaryForm: bytes = None,
                 coordinate: BytecodeArtifactCoordinate = None,
                 hash: str = None,
                 jarContents: List[ClasspathElement] = None,
                 name: str = None):
        super().__init__(binaryForm=binaryForm, hash=hash, name=name)
        self.coordinate = coordinate
        self.jarContents = jarContents
