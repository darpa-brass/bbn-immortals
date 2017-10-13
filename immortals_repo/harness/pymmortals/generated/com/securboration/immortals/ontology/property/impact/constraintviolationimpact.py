from pymmortals.generated.com.securboration.immortals.ontology.constraint.constraintimpacttype import ConstraintImpactType
from pymmortals.generated.com.securboration.immortals.ontology.constraint.directionofviolationtype import DirectionOfViolationType
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.impactstatement import ImpactStatement
from typing import Type


# noinspection PyPep8Naming
class ConstraintViolationImpact(ImpactStatement):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 constraintViolationType: ConstraintImpactType = None,
                 directionOfViolation: DirectionOfViolationType = None,
                 humanReadableDescription: str = None,
                 impactedResource: Type[Resource] = None,
                 violationMessage: str = None):
        super().__init__(humanReadableDescription=humanReadableDescription)
        self.constraintViolationType = constraintViolationType
        self.directionOfViolation = directionOfViolation
        self.impactedResource = impactedResource
        self.violationMessage = violationMessage
