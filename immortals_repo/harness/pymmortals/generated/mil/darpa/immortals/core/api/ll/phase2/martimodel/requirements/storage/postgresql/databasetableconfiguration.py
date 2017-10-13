from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.databasecolumns import DatabaseColumns
from typing import List


# noinspection PyPep8Naming
class DatabaseTableConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 columns: List[DatabaseColumns] = None):
        super().__init__()
        self.columns = columns
