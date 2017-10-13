from pymmortals.generated.com.securboration.immortals.ontology.constraint.valuecriteriontype import ValueCriterionType
from pymmortals.generated.com.securboration.immortals.ontology.metrics.metric import Metric
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.criterionstatement import CriterionStatement


# noinspection PyPep8Naming
class MeasurementCriterion(CriterionStatement):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 criterion: ValueCriterionType = None,
                 humanReadableDescription: str = None,
                 measuredMetricValue: Metric = None):
        super().__init__(humanReadableDescription=humanReadableDescription)
        self.criterion = criterion
        self.measuredMetricValue = measuredMetricValue
