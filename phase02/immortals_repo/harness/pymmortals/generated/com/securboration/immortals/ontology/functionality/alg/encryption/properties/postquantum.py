from pymmortals.generated.com.securboration.immortals.ontology.algorithm.algorithmproperty import AlgorithmProperty
from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint


# noinspection PyPep8Naming
class PostQuantum(AlgorithmProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 truthConstraint: TruthConstraint = None):
        super().__init__(truthConstraint=truthConstraint)
