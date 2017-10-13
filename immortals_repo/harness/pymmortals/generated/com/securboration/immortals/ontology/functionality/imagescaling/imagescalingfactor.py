from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property


# noinspection PyPep8Naming
class ImageScalingFactor(Property):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 scalingFactor: float = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(truthConstraint=truthConstraint)
        self.scalingFactor = scalingFactor
