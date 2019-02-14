from pymmortals.generated.com.securboration.immortals.ontology.constraint.constraintcriteriontype import ConstraintCriterionType
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.criterionstatement import CriterionStatement
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.proscriptivecauseeffectassertion import ProscriptiveCauseEffectAssertion


# noinspection PyPep8Naming
class ConstraintViolationCriterion(CriterionStatement):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 constraint: ProscriptiveCauseEffectAssertion = None,
                 humanReadableDescription: str = None,
                 triggeringConstraintCriterion: ConstraintCriterionType = None):
        super().__init__(humanReadableDescription=humanReadableDescription)
        self.constraint = constraint
        self.triggeringConstraintCriterion = triggeringConstraintCriterion
