from pymmortals.generated.com.securboration.immortals.ontology.bytecode.application.classpath import Classpath
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.classpathelement import ClasspathElement
from typing import List
from typing import Set


# noinspection PyPep8Naming
class NamedClasspath(Classpath):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 classpathName: str = None,
                 element: List[ClasspathElement] = None,
                 elementHashValues: Set[str] = None):
        super().__init__(element=element)
        self.classpathName = classpathName
        self.elementHashValues = elementHashValues
