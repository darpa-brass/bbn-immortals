from pymmortals.generated.com.securboration.immortals.ontology.bytecode.classartifact import ClassArtifact
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.classpathelement import ClasspathElement
from pymmortals.generated.com.securboration.immortals.ontology.java.vcs.vcscoordinate import VcsCoordinate
from typing import List


# noinspection PyPep8Naming
class CompiledJavaSourceFile(ClasspathElement):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 binaryForm: bytes = None,
                 correspondingClass: List[ClassArtifact] = None,
                 hash: str = None,
                 name: str = None,
                 sourceEncoding: str = None,
                 vcsInfo: VcsCoordinate = None):
        super().__init__(binaryForm=binaryForm, hash=hash, name=name)
        self.correspondingClass = correspondingClass
        self.sourceEncoding = sourceEncoding
        self.vcsInfo = vcsInfo
