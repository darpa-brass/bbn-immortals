from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.assertionbindingsite import AssertionBindingSite
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.criterionstatement import CriterionStatement
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.impactstatement import ImpactStatement
from typing import List
from typing import Type


# noinspection PyPep8Naming
class CauseEffectAssertion(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 applicableDataType: Type[DataType] = None,
                 assertionBindingSite: AssertionBindingSite = None,
                 criterion: CriterionStatement = None,
                 humanReadableDescription: str = None,
                 impact: List[ImpactStatement] = None):
        super().__init__()
        self.applicableDataType = applicableDataType
        self.assertionBindingSite = assertionBindingSite
        self.criterion = criterion
        self.humanReadableDescription = humanReadableDescription
        self.impact = impact
