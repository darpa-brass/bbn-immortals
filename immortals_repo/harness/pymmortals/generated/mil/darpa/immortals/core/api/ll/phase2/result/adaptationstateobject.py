from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.adaptationdetails import AdaptationDetails
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.dasoutcome import DasOutcome


# noinspection PyPep8Naming
class AdaptationStateObject(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 adaptationStatus: DasOutcome = None,
                 details: AdaptationDetails = None):
        super().__init__()
        self.adaptationStatus = adaptationStatus
        self.details = details
