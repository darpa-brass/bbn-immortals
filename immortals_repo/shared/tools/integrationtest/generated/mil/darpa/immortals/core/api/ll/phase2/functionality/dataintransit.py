from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.functionality.securitystandard import SecurityStandard


# noinspection PyPep8Naming
class DataInTransit(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 securityStandard: SecurityStandard = None):
        super().__init__()
        self.securityStandard = securityStandard
