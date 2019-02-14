from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.androidresource import AndroidResource
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.atakliterequirements import AtakliteRequirements
from typing import List


# noinspection PyPep8Naming
class ATAKLiteSubmissionModel(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 requirements: AtakliteRequirements = None,
                 resources: List[AndroidResource] = None):
        super().__init__()
        self.requirements = requirements
        self.resources = resources
