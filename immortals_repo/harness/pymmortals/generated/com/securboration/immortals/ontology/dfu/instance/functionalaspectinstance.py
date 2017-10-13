from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.dfu.instance.argtosemantictypebinding import ArgToSemanticTypeBinding
from pymmortals.generated.com.securboration.immortals.ontology.dfu.instance.returnvaluetosemantictypebinding import ReturnValueToSemanticTypeBinding
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionalaspect import FunctionalAspect
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class FunctionalAspectInstance(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 abstractAspect: Type[FunctionalAspect] = None,
                 argsToSemanticTypes: List[ArgToSemanticTypeBinding] = None,
                 methodPointer: str = None,
                 properties: List[Property] = None,
                 recipe: str = None,
                 resourceDependencies: List[Type[Resource]] = None,
                 returnValueToSemanticType: ReturnValueToSemanticTypeBinding = None):
        super().__init__()
        self.abstractAspect = abstractAspect
        self.argsToSemanticTypes = argsToSemanticTypes
        self.methodPointer = methodPointer
        self.properties = properties
        self.recipe = recipe
        self.resourceDependencies = resourceDependencies
        self.returnValueToSemanticType = returnValueToSemanticType
