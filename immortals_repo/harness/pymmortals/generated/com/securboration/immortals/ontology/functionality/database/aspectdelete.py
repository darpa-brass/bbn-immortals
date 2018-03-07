from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.functionality.aspects.defaultaspectbase import DefaultAspectBase
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionalaspect import FunctionalAspect
from pymmortals.generated.com.securboration.immortals.ontology.functionality.input import Input
from pymmortals.generated.com.securboration.immortals.ontology.functionality.output import Output
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.impactstatement import ImpactStatement
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class AspectDelete(DefaultAspectBase):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 aspectId: str = None,
                 aspectProperties: List[Property] = None,
                 aspectSpecificResourceDependencies: List[Type[Resource]] = None,
                 impactStatements: List[ImpactStatement] = None,
                 inputs: List[Input] = None,
                 inverseAspect: Type[FunctionalAspect] = None,
                 outputs: List[Output] = None):
        super().__init__(aspectId=aspectId, aspectProperties=aspectProperties, aspectSpecificResourceDependencies=aspectSpecificResourceDependencies, impactStatements=impactStatements, inputs=inputs, inverseAspect=inverseAspect, outputs=outputs)
