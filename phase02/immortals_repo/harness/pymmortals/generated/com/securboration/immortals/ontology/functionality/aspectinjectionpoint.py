from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.functionality.aspectinjectionrelation import AspectInjectionRelation
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionalaspect import FunctionalAspect
from typing import Type


# noinspection PyPep8Naming
class AspectInjectionPoint(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 aspect: Type[FunctionalAspect] = None,
                 aspectInjectionRelation: AspectInjectionRelation = None):
        super().__init__()
        self.aspect = aspect
        self.aspectInjectionRelation = aspectInjectionRelation
