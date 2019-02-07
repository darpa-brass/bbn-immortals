from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase1.status import Status
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase1.testresult import TestResult


# noinspection PyPep8Naming
class TestDetails(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 actualStatus: Status = None,
                 details: TestResult = None,
                 expectedStatus: Status = None,
                 testIdentifier: str = None):
        super().__init__()
        self.actualStatus = actualStatus
        self.details = details
        self.expectedStatus = expectedStatus
        self.testIdentifier = testIdentifier
