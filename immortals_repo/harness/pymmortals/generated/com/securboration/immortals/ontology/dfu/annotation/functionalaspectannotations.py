from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.dfu.annotation.functionalaspectannotation import FunctionalAspectAnnotation
from typing import List


# noinspection PyPep8Naming
class FunctionalAspectAnnotations(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 aspects: List[FunctionalAspectAnnotation] = None):
        super().__init__()
        self.aspects = aspects
