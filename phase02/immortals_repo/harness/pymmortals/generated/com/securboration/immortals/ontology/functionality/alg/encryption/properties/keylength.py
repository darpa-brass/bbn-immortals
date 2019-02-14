from pymmortals.generated.com.securboration.immortals.ontology.algorithm.algorithmconfigurationproperty import AlgorithmConfigurationProperty
from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint


# noinspection PyPep8Naming
class KeyLength(AlgorithmConfigurationProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 keyLength: int = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(truthConstraint=truthConstraint)
        self.keyLength = keyLength
