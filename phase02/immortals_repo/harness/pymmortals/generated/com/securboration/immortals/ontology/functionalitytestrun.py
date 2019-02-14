from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.callgraph import CallGraph


# noinspection PyPep8Naming
class FunctionalityTestRun(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 callGraph: CallGraph = None,
                 success: bool = None,
                 testBeginTime: str = None,
                 testEndTime: str = None):
        super().__init__()
        self.callGraph = callGraph
        self.success = success
        self.testBeginTime = testBeginTime
        self.testEndTime = testEndTime
