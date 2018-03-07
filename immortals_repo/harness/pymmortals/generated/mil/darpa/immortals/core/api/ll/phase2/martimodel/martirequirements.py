from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.serverpartialupgradelibrary import ServerPartialUpgradeLibrary
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.serverupgradelibrary import ServerUpgradeLibrary
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.databaseperturbation import DatabasePerturbation


# noinspection PyPep8Naming
class MartiRequirements(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 libraryUpgrade: ServerUpgradeLibrary = None,
                 partialLibraryUpgrade: ServerPartialUpgradeLibrary = None,
                 postgresqlPerturbation: DatabasePerturbation = None):
        super().__init__()
        self.libraryUpgrade = libraryUpgrade
        self.partialLibraryUpgrade = partialLibraryUpgrade
        self.postgresqlPerturbation = postgresqlPerturbation
