from pymmortals.generated.com.securboration.immortals.ontology.bytecode.aclass import AClass
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.bytecodeartifact import BytecodeArtifact


# noinspection PyPep8Naming
class ClassArtifact(BytecodeArtifact):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 binaryForm: bytes = None,
                 classModel: AClass = None,
                 hash: str = None,
                 name: str = None):
        super().__init__(binaryForm=binaryForm, hash=hash, name=name)
        self.classModel = classModel
