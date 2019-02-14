from pymmortals.datatypes.serializable import Serializable
from typing import List


# noinspection PyPep8Naming
class ImmortalsContext(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 graphs: List[str] = None):
        super().__init__()
        self.graphs = graphs
