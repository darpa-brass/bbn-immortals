from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.functionality.dataintransit import DataInTransit


# noinspection PyPep8Naming
class GlobalRequirements(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 dataInTransit: DataInTransit = None):
        super().__init__()
        self.dataInTransit = dataInTransit
