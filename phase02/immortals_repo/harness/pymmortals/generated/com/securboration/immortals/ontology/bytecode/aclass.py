from pymmortals.generated.com.securboration.immortals.ontology.bytecode.aclass import AClass
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.afield import AField
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.amethod import AMethod
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.anannotation import AnAnnotation
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.bytecodeversion import BytecodeVersion
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.classstructure import ClassStructure
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.modifier import Modifier
from typing import List


# noinspection PyPep8Naming
class AClass(ClassStructure):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 annotations: List[AnAnnotation] = None,
                 bytecodePointer: str = None,
                 bytecodeVersion: BytecodeVersion = None,
                 className: str = None,
                 classUrl: str = None,
                 fields: List[AField] = None,
                 innerClasses: List['AClass'] = None,
                 methods: List[AMethod] = None,
                 modifiers: List[Modifier] = None,
                 owner: 'AClass' = None,
                 sourceUrl: str = None):
        super().__init__(annotations=annotations, modifiers=modifiers, owner=owner)
        self.bytecodePointer = bytecodePointer
        self.bytecodeVersion = bytecodeVersion
        self.className = className
        self.classUrl = classUrl
        self.fields = fields
        self.innerClasses = innerClasses
        self.methods = methods
        self.sourceUrl = sourceUrl
