from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientpartialupgradelibrary import ClientPartialUpgradeLibrary


# noinspection PyPep8Naming
class ClientPartialLibraryUpgradeRequirements(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 libraryIdentifier: ClientPartialUpgradeLibrary = None,
                 libraryVersion: str = None):
        super().__init__()
        self.libraryIdentifier = libraryIdentifier
        self.libraryVersion = libraryVersion
