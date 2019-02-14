from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.cp.functionalityspec import FunctionalitySpec
from pymmortals.generated.com.securboration.immortals.ontology.cp.missionspec import MissionSpec
from typing import List


# noinspection PyPep8Naming
class GmeInterchangeFormat(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 availableResources: List[Resource] = None,
                 functionalitySpec: List[FunctionalitySpec] = None,
                 humanReadableDescription: str = None,
                 missionSpec: List[MissionSpec] = None,
                 sessionIdentifier: str = None):
        super().__init__()
        self.availableResources = availableResources
        self.functionalitySpec = functionalitySpec
        self.humanReadableDescription = humanReadableDescription
        self.missionSpec = missionSpec
        self.sessionIdentifier = sessionIdentifier
