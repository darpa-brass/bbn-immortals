from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.dfu.annotation.resourcedependent import ResourceDependent
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionalaspect import FunctionalAspect
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionality import Functionality
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class DfuAnnotation(ResourceDependent):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 functionalAspects: List[Type[FunctionalAspect]] = None,
                 functionalityBeingPerformed: Type[Functionality] = None,
                 properties: List[Property] = None,
                 resourceDependencies: List[Type[Resource]] = None,
                 resourceDependencyUris: List[str] = None,
                 tag: str = None):
        super().__init__()
        self.functionalAspects = functionalAspects
        self.functionalityBeingPerformed = functionalityBeingPerformed
        self.properties = properties
        self.resourceDependencies = resourceDependencies
        self.resourceDependencyUris = resourceDependencyUris
        self.tag = tag
