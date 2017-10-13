from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.functionalitytestrun import FunctionalityTestRun
from pymmortals.generated.com.securboration.immortals.ontology.java.compiler.namedclasspath import NamedClasspath
from typing import List


# noinspection PyPep8Naming
class FunctionalityCheck(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 classpath: NamedClasspath = None,
                 functionalityTestRuns: List[FunctionalityTestRun] = None,
                 methodPointer: str = None,
                 type: str = None):
        super().__init__()
        self.classpath = classpath
        self.functionalityTestRuns = functionalityTestRuns
        self.methodPointer = methodPointer
        self.type = type
