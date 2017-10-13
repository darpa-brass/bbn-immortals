from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.time.temporalproperty import TemporalProperty
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.time.time import Time


# noinspection PyPep8Naming
class TimeProperty(TemporalProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 hidden: bool = None,
                 time: Time = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(hidden=hidden, truthConstraint=truthConstraint)
        self.time = time
