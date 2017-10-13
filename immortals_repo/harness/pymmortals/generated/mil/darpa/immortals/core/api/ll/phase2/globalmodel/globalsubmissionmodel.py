from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.globalmodel.globalrequirements import GlobalRequirements


# noinspection PyPep8Naming
class GlobalSubmissionModel(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 requirements: GlobalRequirements = None):
        super().__init__()
        self.requirements = requirements
