from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.dataproperty import DataProperty


# noinspection PyPep8Naming
class NumberOfPixels(DataProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 heightPixels: int = None,
                 hidden: bool = None,
                 totalPixels: int = None,
                 truthConstraint: TruthConstraint = None,
                 widthPixels: int = None):
        super().__init__(hidden=hidden, truthConstraint=truthConstraint)
        self.heightPixels = heightPixels
        self.totalPixels = totalPixels
        self.widthPixels = widthPixels
