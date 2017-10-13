from pymmortals.generated.com.securboration.immortals.ontology.bytecode.aclass import AClass
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.anannotation import AnAnnotation
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.classstructure import ClassStructure
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.modifier import Modifier
from typing import List


# noinspection PyPep8Naming
class AField(ClassStructure):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 annotations: List[AnAnnotation] = None,
                 bytecodePointer: str = None,
                 fieldDesc: str = None,
                 fieldName: str = None,
                 modifiers: List[Modifier] = None,
                 owner: AClass = None):
        super().__init__(annotations=annotations, modifiers=modifiers, owner=owner)
        self.bytecodePointer = bytecodePointer
        self.fieldDesc = fieldDesc
        self.fieldName = fieldName
