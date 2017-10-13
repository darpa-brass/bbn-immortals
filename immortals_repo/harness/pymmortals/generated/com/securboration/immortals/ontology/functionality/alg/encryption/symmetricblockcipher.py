from pymmortals.generated.com.securboration.immortals.ontology.algorithm.algorithm import Algorithm
from pymmortals.generated.com.securboration.immortals.ontology.algorithm.algorithmproperty import AlgorithmProperty
from pymmortals.generated.com.securboration.immortals.ontology.algorithm.algorithmstandardproperty import AlgorithmStandardProperty
from pymmortals.generated.com.securboration.immortals.ontology.functionality.alg.encryption.properties.blockbased import BlockBased
from pymmortals.generated.com.securboration.immortals.ontology.functionality.alg.encryption.properties.keylength import KeyLength
from typing import List


# noinspection PyPep8Naming
class SymmetricBlockCipher(Algorithm):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 blockSpec: BlockBased = None,
                 encryptionSpec: AlgorithmStandardProperty = None,
                 keySpec: KeyLength = None,
                 properties: List[AlgorithmProperty] = None):
        super().__init__(properties=properties)
        self.blockSpec = blockSpec
        self.encryptionSpec = encryptionSpec
        self.keySpec = keySpec
