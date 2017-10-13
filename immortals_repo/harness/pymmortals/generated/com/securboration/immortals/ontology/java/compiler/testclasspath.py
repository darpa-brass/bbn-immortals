from pymmortals.generated.com.securboration.immortals.ontology.bytecode.application.classpath import Classpath
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.classpathelement import ClasspathElement
from typing import List


# noinspection PyPep8Naming
class TestClasspath(Classpath):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 element: List[ClasspathElement] = None):
        super().__init__(element=element)
