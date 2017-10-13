from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.bytecodeartifactcoordinate import BytecodeArtifactCoordinate
from pymmortals.generated.com.securboration.immortals.ontology.java.source.buildmechanism import BuildMechanism
from pymmortals.generated.com.securboration.immortals.ontology.java.source.javasourcefile import JavaSourceFile
from typing import List


# noinspection PyPep8Naming
class JavaProject(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 buildMechanism: BuildMechanism = None,
                 producesArtifact: BytecodeArtifactCoordinate = None,
                 sourceFiles: List[JavaSourceFile] = None):
        super().__init__()
        self.buildMechanism = buildMechanism
        self.producesArtifact = producesArtifact
        self.sourceFiles = sourceFiles
