from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.dasoutcome import DasOutcome


# noinspection PyPep8Naming
class AbstractAdaptationDetails(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 adaptationIdentifier: str = None,
                 dasOutcome: DasOutcome = None):
        super().__init__()
        self.adaptationIdentifier = adaptationIdentifier
        self.dasOutcome = dasOutcome
