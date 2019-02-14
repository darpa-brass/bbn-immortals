from pymmortals.generated.com.securboration.immortals.ontology.algorithm.algorithmspecificationproperty import AlgorithmSpecificationProperty
from pymmortals.generated.com.securboration.immortals.ontology.algorithm.purpose.algorithmpurpose import AlgorithmPurpose
from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint


# noinspection PyPep8Naming
class AlgorithmPurposeProperty(AlgorithmSpecificationProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 purpose: AlgorithmPurpose = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(truthConstraint=truthConstraint)
        self.purpose = purpose
