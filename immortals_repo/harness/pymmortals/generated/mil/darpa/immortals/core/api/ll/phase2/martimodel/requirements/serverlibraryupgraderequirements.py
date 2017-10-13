from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.serverupgradelibrary import ServerUpgradeLibrary


# noinspection PyPep8Naming
class ServerLibraryUpgradeRequirements(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 libraryIdentifier: ServerUpgradeLibrary = None,
                 libraryVersion: str = None):
        super().__init__()
        self.libraryIdentifier = libraryIdentifier
        self.libraryVersion = libraryVersion
