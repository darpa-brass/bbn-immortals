from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.bytecodeversion import BytecodeVersion
from typing import List


# noinspection PyPep8Naming
class JavaSourceVersion(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 backwardCompatibleWith: List['JavaSourceVersion'] = None,
                 targetBytecodeVersion: BytecodeVersion = None):
        super().__init__()
        self.backwardCompatibleWith = backwardCompatibleWith
        self.targetBytecodeVersion = targetBytecodeVersion
