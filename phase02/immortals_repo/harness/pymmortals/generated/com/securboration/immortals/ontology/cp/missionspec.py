from pymmortals.generated.com.securboration.immortals.ontology.constraint.valuecriteriontype import ValueCriterionType
from pymmortals.generated.com.securboration.immortals.ontology.cp.softwarespec import SoftwareSpec
from pymmortals.generated.com.securboration.immortals.ontology.metrics.metric import Metric
from pymmortals.generated.com.securboration.immortals.ontology.ordering.explicitnumericorderingmechanism import ExplicitNumericOrderingMechanism


# noinspection PyPep8Naming
class MissionSpec(SoftwareSpec):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 assertionCriterion: ValueCriterionType = None,
                 humanReadableForm: str = None,
                 precedenceOfSpec: ExplicitNumericOrderingMechanism = None,
                 rightValue: Metric = None):
        super().__init__(precedenceOfSpec=precedenceOfSpec)
        self.assertionCriterion = assertionCriterion
        self.humanReadableForm = humanReadableForm
        self.rightValue = rightValue
