from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.dfu.annotation.resourcedependent import ResourceDependent
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionalaspect import FunctionalAspect
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class FunctionalAspectAnnotation(ResourceDependent):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 aspect: Type[FunctionalAspect] = None,
                 aspectSpecificResourceDependencies: List[Type[Resource]] = None,
                 properties: List[Property] = None,
                 resourceDependencyUris: List[str] = None):
        super().__init__()
        self.aspect = aspect
        self.aspectSpecificResourceDependencies = aspectSpecificResourceDependencies
        self.properties = properties
        self.resourceDependencyUris = resourceDependencyUris
