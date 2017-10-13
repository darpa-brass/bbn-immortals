from pymmortals.generated.com.securboration.immortals.ontology.constraint.resourceimpacttype import ResourceImpactType
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.impactstatement import ImpactStatement
from typing import Type


# noinspection PyPep8Naming
class ResourceImpact(ImpactStatement):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 impactOnResource: ResourceImpactType = None,
                 impactedResource: Type[Resource] = None):
        super().__init__(humanReadableDescription=humanReadableDescription)
        self.impactOnResource = impactOnResource
        self.impactedResource = impactedResource
