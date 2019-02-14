from pymmortals.generated.com.securboration.immortals.ontology.property.impact.impactstatement import ImpactStatement
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.predictivecauseeffectassertion import PredictiveCauseEffectAssertion


# noinspection PyPep8Naming
class RemediationImpact(ImpactStatement):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 remediationStrategy: PredictiveCauseEffectAssertion = None):
        super().__init__(humanReadableDescription=humanReadableDescription)
        self.remediationStrategy = remediationStrategy
