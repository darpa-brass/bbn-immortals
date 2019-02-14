from pymmortals.generated.com.securboration.immortals.ontology.bytecode.aclass import AClass
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.anannotation import AnAnnotation
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.classstructure import ClassStructure
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.modifier import Modifier
from typing import List


# noinspection PyPep8Naming
class AnnotationKeyValuePair(ClassStructure):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 annotations: List[AnAnnotation] = None,
                 key: str = None,
                 modifiers: List[Modifier] = None,
                 owner: AClass = None,
                 value: str = None):
        super().__init__(annotations=annotations, modifiers=modifiers, owner=owner)
        self.key = key
        self.value = value
