from pymmortals.generated.com.securboration.immortals.ontology.bytecode.bytecodeartifact import BytecodeArtifact
from pymmortals.generated.com.securboration.immortals.ontology.identifier.hasuuid import HasUuid
from pymmortals.generated.com.securboration.immortals.ontology.java.build.buildscript import BuildScript
from pymmortals.generated.com.securboration.immortals.ontology.java.compiler.namedclasspath import NamedClasspath
from pymmortals.generated.com.securboration.immortals.ontology.java.source.compiledjavasourcefile import CompiledJavaSourceFile
from typing import List


# noinspection PyPep8Naming
class JavaProject(HasUuid):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 buildScript: BuildScript = None,
                 classpaths: List[NamedClasspath] = None,
                 compiledSoftware: BytecodeArtifact = None,
                 compiledSourceHash: List[str] = None,
                 compiledTestSource: List[CompiledJavaSourceFile] = None,
                 uuid: str = None):
        super().__init__()
        self.buildScript = buildScript
        self.classpaths = classpaths
        self.compiledSoftware = compiledSoftware
        self.compiledSourceHash = compiledSourceHash
        self.compiledTestSource = compiledTestSource
        self.uuid = uuid
