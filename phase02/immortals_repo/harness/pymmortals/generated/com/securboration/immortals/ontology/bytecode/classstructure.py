from pymmortals.generated.com.securboration.immortals.ontology.bytecode.aclass import AClass
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.anannotation import AnAnnotation
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.modifier import Modifier
from pymmortals.generated.com.securboration.immortals.ontology.lang.compiledcodeunit import CompiledCodeUnit
from typing import List


# noinspection PyPep8Naming
class ClassStructure(CompiledCodeUnit):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 annotations: List[AnAnnotation] = None,
                 modifiers: List[Modifier] = None,
                 owner: AClass = None):
        super().__init__()
        self.annotations = annotations
        self.modifiers = modifiers
        self.owner = owner
