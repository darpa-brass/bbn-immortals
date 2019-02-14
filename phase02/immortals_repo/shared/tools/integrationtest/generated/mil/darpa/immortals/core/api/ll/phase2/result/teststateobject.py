from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.testoutcome import TestOutcome
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.testdetails import TestDetails


# noinspection PyPep8Naming
class TestStateObject(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 actualStatus: TestOutcome = None,
                 desiredStatus: TestOutcome = None,
                 details: TestDetails = None,
                 intent: str = None,
                 testIdentifier: str = None):
        super().__init__()
        self.actualStatus = actualStatus
        self.desiredStatus = desiredStatus
        self.details = details
        self.intent = intent
        self.testIdentifier = testIdentifier
