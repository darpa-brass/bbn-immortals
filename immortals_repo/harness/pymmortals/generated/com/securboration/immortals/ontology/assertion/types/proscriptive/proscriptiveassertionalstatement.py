from pymmortals.generated.com.securboration.immortals.ontology.assertion.assertionalstatement import AssertionalStatement
from pymmortals.generated.com.securboration.immortals.ontology.assertion.binding.bindingsitebase import BindingSiteBase
from pymmortals.generated.com.securboration.immortals.ontology.expression.booleanexpression import BooleanExpression
from pymmortals.generated.com.securboration.immortals.ontology.impact.constraint.violationimpact import ViolationImpact


# noinspection PyPep8Naming
class ProscriptiveAssertionalStatement(AssertionalStatement):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 impactOfViolation: ViolationImpact = None,
                 proscriptiveAssertionCriterion: BooleanExpression = None,
                 subjectOfProscriptiveAssertion: BindingSiteBase = None):
        super().__init__()
        self.impactOfViolation = impactOfViolation
        self.proscriptiveAssertionCriterion = proscriptiveAssertionCriterion
        self.subjectOfProscriptiveAssertion = subjectOfProscriptiveAssertion
