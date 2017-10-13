from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionalaspect import FunctionalAspect
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionality import Functionality
from pymmortals.generated.com.securboration.immortals.ontology.pattern.spec.abstractusageparadigm import AbstractUsageParadigm
from pymmortals.generated.com.securboration.immortals.ontology.pattern.spec.speccomponent import SpecComponent
from typing import List
from typing import Type


# noinspection PyPep8Naming
class LibraryFunctionalAspectSpec(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 aspect: Type[FunctionalAspect] = None,
                 component: List[SpecComponent] = None,
                 durableId: str = None,
                 functionality: Type[Functionality] = None,
                 libraryCoordinateTag: str = None,
                 usageParadigm: AbstractUsageParadigm = None):
        super().__init__()
        self.aspect = aspect
        self.component = component
        self.durableId = durableId
        self.functionality = functionality
        self.libraryCoordinateTag = libraryCoordinateTag
        self.usageParadigm = usageParadigm
