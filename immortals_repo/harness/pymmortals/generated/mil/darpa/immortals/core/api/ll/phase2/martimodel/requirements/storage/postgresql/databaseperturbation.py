from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.databasetableconfiguration import DatabaseTableConfiguration
from typing import List


# noinspection PyPep8Naming
class DatabasePerturbation(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 tables: List[DatabaseTableConfiguration] = None):
        super().__init__()
        self.tables = tables
