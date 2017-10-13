from pymmortals.generated.com.securboration.immortals.ontology.bytecode.aclass import AClass
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.anannotation import AnAnnotation
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.annotationkeyvaluepair import AnnotationKeyValuePair
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.classstructure import ClassStructure
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.modifier import Modifier
from typing import List


# noinspection PyPep8Naming
class AnAnnotation(ClassStructure):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 annotationClassName: str = None,
                 annotations: List['AnAnnotation'] = None,
                 keyValuePairs: List[AnnotationKeyValuePair] = None,
                 modifiers: List[Modifier] = None,
                 owner: AClass = None):
        super().__init__(annotations=annotations, modifiers=modifiers, owner=owner)
        self.annotationClassName = annotationClassName
        self.keyValuePairs = keyValuePairs
