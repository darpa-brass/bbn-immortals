from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.serverlibraryupgraderequirements import ServerLibraryUpgradeRequirements
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.serverpartiallibraryupgraderequirements import ServerPartialLibraryUpgradeRequirements
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.databaseperturbation import DatabasePerturbation


# noinspection PyPep8Naming
class MartiRequirements(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 libraryUpgrade: ServerLibraryUpgradeRequirements = None,
                 partialLibraryUpgrade: ServerPartialLibraryUpgradeRequirements = None,
                 postgresqlPerturbation: DatabasePerturbation = None):
        super().__init__()
        self.libraryUpgrade = libraryUpgrade
        self.partialLibraryUpgrade = partialLibraryUpgrade
        self.postgresqlPerturbation = postgresqlPerturbation
