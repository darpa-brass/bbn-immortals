from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.verdictoutcome import VerdictOutcome


# noinspection PyPep8Naming
class IntentObject(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 intentIdentifier: str = None,
                 verdictOutcome: VerdictOutcome = None):
        super().__init__()
        self.intentIdentifier = intentIdentifier
        self.verdictOutcome = verdictOutcome
