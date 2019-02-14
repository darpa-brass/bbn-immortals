from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.adaptationstateobject import AdaptationStateObject
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.validationstateobject import ValidationStateObject


# noinspection PyPep8Naming
class TestAdapterState(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 adaptation: AdaptationStateObject = None,
                 identifier: str = None,
                 sequence: int = None,
                 validation: ValidationStateObject = None):
        super().__init__()
        self.adaptation = adaptation
        self.identifier = identifier
        self.sequence = sequence
        self.validation = validation
