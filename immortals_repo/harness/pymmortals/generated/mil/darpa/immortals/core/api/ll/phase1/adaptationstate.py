from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.adaptationresult import AdaptationResult
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.status import Status


# noinspection PyPep8Naming
class AdaptationState(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 adaptationStatus: Status = None,
                 details: AdaptationResult = None):
        super().__init__()
        self.adaptationStatus = adaptationStatus
        self.details = details
