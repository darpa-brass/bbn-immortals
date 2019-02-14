from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint


# noinspection PyPep8Naming
class Property(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 truthConstraint: TruthConstraint = None):
        super().__init__()
        self.truthConstraint = truthConstraint
