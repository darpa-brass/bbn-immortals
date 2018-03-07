from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.proscriptivecauseeffectassertion import ProscriptiveCauseEffectAssertion
from typing import List


# noinspection PyPep8Naming
class RevisedGmeInterchangeFormat(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 availableResources: List[Resource] = None,
                 constraints: List[ProscriptiveCauseEffectAssertion] = None):
        super().__init__()
        self.availableResources = availableResources
        self.constraints = constraints
