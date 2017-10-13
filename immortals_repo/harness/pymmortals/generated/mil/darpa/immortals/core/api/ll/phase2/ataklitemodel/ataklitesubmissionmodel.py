from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.atakliterequirements import AtakliteRequirements


# noinspection PyPep8Naming
class ATAKLiteSubmissionModel(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 requirements: AtakliteRequirements = None):
        super().__init__()
        self.requirements = requirements
