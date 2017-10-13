from pymmortals.generated.com.securboration.immortals.ontology.algorithm.algorithmconfigurationproperty import AlgorithmConfigurationProperty
from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint


# noinspection PyPep8Naming
class AlgorithmStandardProperty(AlgorithmConfigurationProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 ownerOrganization: str = None,
                 standardName: str = None,
                 truthConstraint: TruthConstraint = None,
                 url: str = None):
        super().__init__(truthConstraint=truthConstraint)
        self.ownerOrganization = ownerOrganization
        self.standardName = standardName
        self.url = url
