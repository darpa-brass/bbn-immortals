from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.applications.applicationtype import ApplicationType


# noinspection PyPep8Naming
class CreateApplicationInstanceData(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 applicationType: ApplicationType = None,
                 sessionIdentifier: str = None):
        super().__init__()
        self.applicationType = applicationType
        self.sessionIdentifier = sessionIdentifier
