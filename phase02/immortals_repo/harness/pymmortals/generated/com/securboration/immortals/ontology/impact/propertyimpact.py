from pymmortals.generated.com.securboration.immortals.ontology.constraint.propertyimpacttype import PropertyImpactType
from pymmortals.generated.com.securboration.immortals.ontology.impact.impactstatement import ImpactStatement
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import Type


# noinspection PyPep8Naming
class PropertyImpact(ImpactStatement):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 impactOnProperty: PropertyImpactType = None,
                 impactedProperty: Type[Property] = None):
        super().__init__()
        self.impactOnProperty = impactOnProperty
        self.impactedProperty = impactedProperty
