from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.dataproperty import DataProperty


# noinspection PyPep8Naming
class Entropy(DataProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 hidden: bool = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(hidden=hidden, truthConstraint=truthConstraint)
