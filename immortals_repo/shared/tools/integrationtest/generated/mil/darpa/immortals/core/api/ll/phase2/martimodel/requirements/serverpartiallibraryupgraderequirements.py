from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.serverpartialupgradelibrary import ServerPartialUpgradeLibrary


# noinspection PyPep8Naming
class ServerPartialLibraryUpgradeRequirements(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 libraryIdentifier: ServerPartialUpgradeLibrary = None,
                 libraryVersion: str = None):
        super().__init__()
        self.libraryIdentifier = libraryIdentifier
        self.libraryVersion = libraryVersion
