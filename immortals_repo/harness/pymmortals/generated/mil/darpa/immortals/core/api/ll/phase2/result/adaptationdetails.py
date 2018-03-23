from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.abstractadaptationdetails import AbstractAdaptationDetails
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.dasoutcome import DasOutcome
from typing import List


# noinspection PyPep8Naming
class AdaptationDetails(AbstractAdaptationDetails):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 adaptationIdentifier: str = None,
                 adaptorIdentifier: str = None,
                 dasOutcome: DasOutcome = None,
                 detailMessages: List[str] = None,
                 errorMessages: List[str] = None,
                 timestamp: int = None):
        super().__init__(adaptationIdentifier=adaptationIdentifier, adaptorIdentifier=adaptorIdentifier, dasOutcome=dasOutcome, timestamp=timestamp)
        self.detailMessages = detailMessages
        self.errorMessages = errorMessages
