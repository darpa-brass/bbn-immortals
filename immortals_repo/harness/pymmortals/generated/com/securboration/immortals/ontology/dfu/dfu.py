from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionality import Functionality
from pymmortals.generated.com.securboration.immortals.ontology.lang.compiledcodeunit import CompiledCodeUnit
from typing import List
from typing import Type


# noinspection PyPep8Naming
class Dfu(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 codeUnit: CompiledCodeUnit = None,
                 functionalityBeingPerformed: Functionality = None,
                 resourceDependencies: List[Type[Resource]] = None):
        super().__init__()
        self.codeUnit = codeUnit
        self.functionalityBeingPerformed = functionalityBeingPerformed
        self.resourceDependencies = resourceDependencies
