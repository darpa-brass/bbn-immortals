from pymmortals.generated.com.securboration.immortals.ontology.connectivity.plireportrate import PliReportRate
from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint


# noinspection PyPep8Naming
class LocationReportRate(PliReportRate):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 reportsPerMinute: float = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(reportsPerMinute=reportsPerMinute, truthConstraint=truthConstraint)
