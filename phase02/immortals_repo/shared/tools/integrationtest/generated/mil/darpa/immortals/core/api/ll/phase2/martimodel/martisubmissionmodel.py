from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.javaresource import JavaResource
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.martirequirements import MartiRequirements
from typing import List


# noinspection PyPep8Naming
class MartiSubmissionModel(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 requirements: MartiRequirements = None,
                 resources: List[JavaResource] = None):
        super().__init__()
        self.requirements = requirements
        self.resources = resources
