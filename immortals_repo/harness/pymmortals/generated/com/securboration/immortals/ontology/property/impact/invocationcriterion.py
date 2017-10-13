from pymmortals.generated.com.securboration.immortals.ontology.constraint.invocationcriteriontype import InvocationCriterionType
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionalaspect import FunctionalAspect
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.criterionstatement import CriterionStatement
from typing import Type


# noinspection PyPep8Naming
class InvocationCriterion(CriterionStatement):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 criterion: InvocationCriterionType = None,
                 humanReadableDescription: str = None,
                 invokedAspect: Type[FunctionalAspect] = None):
        super().__init__(humanReadableDescription=humanReadableDescription)
        self.criterion = criterion
        self.invokedAspect = invokedAspect
