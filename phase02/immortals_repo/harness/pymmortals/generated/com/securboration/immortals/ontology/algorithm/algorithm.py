from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.algorithm.algorithmproperty import AlgorithmProperty
from typing import List


# noinspection PyPep8Naming
class Algorithm(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 properties: List[AlgorithmProperty] = None):
        super().__init__()
        self.properties = properties
