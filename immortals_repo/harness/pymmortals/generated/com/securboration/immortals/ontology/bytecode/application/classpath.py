from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.classpathelement import ClasspathElement
from typing import List


# noinspection PyPep8Naming
class Classpath(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 element: List[ClasspathElement] = None):
        super().__init__()
        self.element = element
