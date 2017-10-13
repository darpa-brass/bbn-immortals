from pymmortals.generated.com.securboration.immortals.ontology.constraint.propertycriteriontype import PropertyCriterionType
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.criterionstatement import CriterionStatement
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import Type


# noinspection PyPep8Naming
class AbstractPropertyCriterion(CriterionStatement):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 criterion: PropertyCriterionType = None,
                 humanReadableDescription: str = None,
                 property: Type[Property] = None):
        super().__init__(humanReadableDescription=humanReadableDescription)
        self.criterion = criterion
        self.property = property
