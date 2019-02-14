from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.dynamiccallgraph import DynamicCallGraph
from pymmortals.generated.com.securboration.immortals.ontology.java.compiler.namedclasspath import NamedClasspath
from pymmortals.generated.com.securboration.immortals.ontology.staticcallgraph import StaticCallGraph


# noinspection PyPep8Naming
class CallGraph(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 classpath: NamedClasspath = None,
                 dynamicCallGraph: DynamicCallGraph = None,
                 staticCallGraph: StaticCallGraph = None):
        super().__init__()
        self.classpath = classpath
        self.dynamicCallGraph = dynamicCallGraph
        self.staticCallGraph = staticCallGraph
