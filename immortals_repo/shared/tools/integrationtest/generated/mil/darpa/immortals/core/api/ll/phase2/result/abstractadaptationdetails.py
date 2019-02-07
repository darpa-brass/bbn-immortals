from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.dasoutcome import DasOutcome


# noinspection PyPep8Naming
class AbstractAdaptationDetails(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 adaptationIdentifier: str = None,
                 adaptationValidationsPerformed: int = None,
                 adaptorIdentifier: str = None,
                 dasOutcome: DasOutcome = None,
                 passingAdaptationValidations: int = None):
        super().__init__()
        self.adaptationIdentifier = adaptationIdentifier
        self.adaptationValidationsPerformed = adaptationValidationsPerformed
        self.adaptorIdentifier = adaptorIdentifier
        self.dasOutcome = dasOutcome
        self.passingAdaptationValidations = passingAdaptationValidations
