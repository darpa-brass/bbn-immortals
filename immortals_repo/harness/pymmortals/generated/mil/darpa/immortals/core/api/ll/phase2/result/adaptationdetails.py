from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.abstractadaptationdetails import AbstractAdaptationDetails
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.dasoutcome import DasOutcome


# noinspection PyPep8Naming
class AdaptationDetails(AbstractAdaptationDetails):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 adaptationIdentifier: str = None,
                 dasOutcome: DasOutcome = None,
                 details: str = None):
        super().__init__(adaptationIdentifier=adaptationIdentifier, dasOutcome=dasOutcome)
        self.details = details
