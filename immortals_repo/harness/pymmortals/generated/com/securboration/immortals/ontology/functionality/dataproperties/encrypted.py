from pymmortals.generated.com.securboration.immortals.ontology.algorithm.algorithm import Algorithm
from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.confidentialproperty import ConfidentialProperty
from typing import Type


# noinspection PyPep8Naming
class Encrypted(ConfidentialProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 encryptionAlgorithm: Type[Algorithm] = None,
                 hidden: bool = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(hidden=hidden, truthConstraint=truthConstraint)
        self.encryptionAlgorithm = encryptionAlgorithm
