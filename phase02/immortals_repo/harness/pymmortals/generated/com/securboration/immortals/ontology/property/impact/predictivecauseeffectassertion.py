from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.assertionbindingsite import AssertionBindingSite
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.causeeffectassertion import CauseEffectAssertion
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.criterionstatement import CriterionStatement
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.impactstatement import ImpactStatement
from typing import List
from typing import Type


# noinspection PyPep8Naming
class PredictiveCauseEffectAssertion(CauseEffectAssertion):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 applicableDataType: Type[DataType] = None,
                 assertionBindingSite: AssertionBindingSite = None,
                 criterion: CriterionStatement = None,
                 humanReadableDescription: str = None,
                 impact: List[ImpactStatement] = None):
        super().__init__(applicableDataType=applicableDataType, assertionBindingSite=assertionBindingSite, criterion=criterion, humanReadableDescription=humanReadableDescription, impact=impact)
