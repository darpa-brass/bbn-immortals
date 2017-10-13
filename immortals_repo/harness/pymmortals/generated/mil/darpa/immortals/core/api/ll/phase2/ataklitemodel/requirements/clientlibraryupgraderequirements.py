from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientupgradelibrary import ClientUpgradeLibrary


# noinspection PyPep8Naming
class ClientLibraryUpgradeRequirements(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 libraryIdentifier: ClientUpgradeLibrary = None,
                 libraryVersion: str = None):
        super().__init__()
        self.libraryIdentifier = libraryIdentifier
        self.libraryVersion = libraryVersion
