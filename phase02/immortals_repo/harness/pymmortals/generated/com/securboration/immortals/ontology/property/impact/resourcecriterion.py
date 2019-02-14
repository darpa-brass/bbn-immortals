from pymmortals.generated.com.securboration.immortals.ontology.constraint.resourcecriteriontype import ResourceCriterionType
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.criterionstatement import CriterionStatement
from typing import Type


# noinspection PyPep8Naming
class ResourceCriterion(CriterionStatement):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 criterion: ResourceCriterionType = None,
                 humanReadableDescription: str = None,
                 resource: Type[Resource] = None):
        super().__init__(humanReadableDescription=humanReadableDescription)
        self.criterion = criterion
        self.resource = resource
