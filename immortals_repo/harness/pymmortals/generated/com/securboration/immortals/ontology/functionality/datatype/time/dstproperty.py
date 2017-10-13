from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.time.temporalproperty import TemporalProperty


# noinspection PyPep8Naming
class DstProperty(TemporalProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 hidden: bool = None,
                 truthConstraint: TruthConstraint = None,
                 usesDaylightSavingsTime: bool = None):
        super().__init__(hidden=hidden, truthConstraint=truthConstraint)
        self.usesDaylightSavingsTime = usesDaylightSavingsTime
