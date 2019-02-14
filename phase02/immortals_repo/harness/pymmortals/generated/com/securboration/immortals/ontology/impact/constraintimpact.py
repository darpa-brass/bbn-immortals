from pymmortals.generated.com.securboration.immortals.ontology.impact.constraint.violationimpact import ViolationImpact
from pymmortals.generated.com.securboration.immortals.ontology.impact.impactstatement import ImpactStatement
from pymmortals.generated.com.securboration.immortals.ontology.impact.violation.violationprovenance import ViolationProvenance


# noinspection PyPep8Naming
class ConstraintImpact(ImpactStatement):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 causeOfViolation: ViolationProvenance = None,
                 constraintEmitted: ViolationImpact = None,
                 violationMessage: str = None):
        super().__init__()
        self.causeOfViolation = causeOfViolation
        self.constraintEmitted = constraintEmitted
        self.violationMessage = violationMessage
