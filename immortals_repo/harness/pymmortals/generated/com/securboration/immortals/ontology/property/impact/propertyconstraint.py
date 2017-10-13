from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.constraint.multiplicitytype import MultiplicityType
from pymmortals.generated.com.securboration.immortals.ontology.constraint.propertycriteriontype import PropertyCriterionType
from pymmortals.generated.com.securboration.immortals.ontology.ordering.explicitnumericorderingmechanism import ExplicitNumericOrderingMechanism
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List


# noinspection PyPep8Naming
class PropertyConstraint(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 constrainedProperty: List[Property] = None,
                 constraintCriterion: PropertyCriterionType = None,
                 constraintMultiplicity: MultiplicityType = None,
                 humanReadableForm: str = None,
                 precedenceOfConstraint: ExplicitNumericOrderingMechanism = None):
        super().__init__()
        self.constrainedProperty = constrainedProperty
        self.constraintCriterion = constraintCriterion
        self.constraintMultiplicity = constraintMultiplicity
        self.humanReadableForm = humanReadableForm
        self.precedenceOfConstraint = precedenceOfConstraint
