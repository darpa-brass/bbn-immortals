from pymmortals.generated.com.securboration.immortals.ontology.core.humanreadable import HumanReadable
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionalaspect import FunctionalAspect
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionality import Functionality
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class ManageDatabase(Functionality, HumanReadable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 functionalAspects: List[FunctionalAspect] = None,
                 functionalityId: str = None,
                 functionalityProperties: List[Property] = None,
                 humanReadableDesc: str = None,
                 resourceDependencies: List[Type[Resource]] = None):
        super().__init__(functionalAspects=functionalAspects, functionalityId=functionalityId, functionalityProperties=functionalityProperties, resourceDependencies=resourceDependencies)
        self.humanReadableDesc = humanReadableDesc
