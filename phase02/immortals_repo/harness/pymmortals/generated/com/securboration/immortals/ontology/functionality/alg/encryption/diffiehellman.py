from pymmortals.generated.com.securboration.immortals.ontology.algorithm.algorithm import Algorithm
from pymmortals.generated.com.securboration.immortals.ontology.algorithm.algorithmproperty import AlgorithmProperty
from typing import List


# noinspection PyPep8Naming
class DiffieHellman(Algorithm):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 properties: List[AlgorithmProperty] = None):
        super().__init__(properties=properties)
