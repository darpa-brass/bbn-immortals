from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.dfu.dfu import Dfu
from typing import List


# noinspection PyPep8Naming
class Dfus(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 dfus: List[Dfu] = None):
        super().__init__()
        self.dfus = dfus
