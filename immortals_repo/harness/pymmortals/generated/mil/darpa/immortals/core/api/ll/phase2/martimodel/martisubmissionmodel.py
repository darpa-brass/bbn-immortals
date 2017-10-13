from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.martirequirements import MartiRequirements


# noinspection PyPep8Naming
class MartiSubmissionModel(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 requirements: MartiRequirements = None):
        super().__init__()
        self.requirements = requirements
