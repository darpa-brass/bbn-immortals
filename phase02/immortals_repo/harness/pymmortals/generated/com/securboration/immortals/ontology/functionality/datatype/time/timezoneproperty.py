from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.time.temporalproperty import TemporalProperty


# noinspection PyPep8Naming
class TimezoneProperty(TemporalProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 gmtOffset: int = None,
                 hidden: bool = None,
                 tag: str = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(hidden=hidden, truthConstraint=truthConstraint)
        self.gmtOffset = gmtOffset
        self.tag = tag
