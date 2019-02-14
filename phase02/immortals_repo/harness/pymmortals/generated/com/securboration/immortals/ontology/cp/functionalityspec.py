from pymmortals.generated.com.securboration.immortals.ontology.cp.softwarespec import SoftwareSpec
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionalaspect import FunctionalAspect
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionality import Functionality
from pymmortals.generated.com.securboration.immortals.ontology.ordering.explicitnumericorderingmechanism import ExplicitNumericOrderingMechanism
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.propertyconstraint import PropertyConstraint
from typing import List
from typing import Type


# noinspection PyPep8Naming
class FunctionalitySpec(SoftwareSpec):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 functionalityPerformed: Type[Functionality] = None,
                 functionalityProvided: Type[FunctionalAspect] = None,
                 precedenceOfSpec: ExplicitNumericOrderingMechanism = None,
                 propertyConstraint: List[PropertyConstraint] = None):
        super().__init__(precedenceOfSpec=precedenceOfSpec)
        self.functionalityPerformed = functionalityPerformed
        self.functionalityProvided = functionalityProvided
        self.propertyConstraint = propertyConstraint
