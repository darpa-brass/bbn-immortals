from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.dfu.instance.functionalaspectinstance import FunctionalAspectInstance
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionality import Functionality
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class DfuInstance(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 classPointer: str = None,
                 dfuProperties: List[Property] = None,
                 functionalAspects: List[FunctionalAspectInstance] = None,
                 functionalityAbstraction: Type[Functionality] = None,
                 resourceDependencies: List[Type[Resource]] = None,
                 tag: str = None):
        super().__init__()
        self.classPointer = classPointer
        self.dfuProperties = dfuProperties
        self.functionalAspects = functionalAspects
        self.functionalityAbstraction = functionalityAbstraction
        self.resourceDependencies = resourceDependencies
        self.tag = tag
