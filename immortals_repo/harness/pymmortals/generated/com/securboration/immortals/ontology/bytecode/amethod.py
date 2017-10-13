from pymmortals.generated.com.securboration.immortals.ontology.bytecode.aclass import AClass
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.analysis.instruction import Instruction
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.anannotation import AnAnnotation
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.classstructure import ClassStructure
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.modifier import Modifier
from typing import List


# noinspection PyPep8Naming
class AMethod(ClassStructure):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 annotations: List[AnAnnotation] = None,
                 bytecode: str = None,
                 bytecodePointer: str = None,
                 interestingInstructions: List[Instruction] = None,
                 methodDesc: str = None,
                 methodName: str = None,
                 modifiers: List[Modifier] = None,
                 owner: AClass = None):
        super().__init__(annotations=annotations, modifiers=modifiers, owner=owner)
        self.bytecode = bytecode
        self.bytecodePointer = bytecodePointer
        self.interestingInstructions = interestingInstructions
        self.methodDesc = methodDesc
        self.methodName = methodName
