from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.adaptationstate import AdaptationState
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.analyticsevent import AnalyticsEvent
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.validationstate import ValidationState
from typing import List


# noinspection PyPep8Naming
class TestAdapterState(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 adaptation: AdaptationState = None,
                 identifier: str = None,
                 rawLogData: List[AnalyticsEvent] = None,
                 validation: ValidationState = None):
        super().__init__()
        self.adaptation = adaptation
        self.identifier = identifier
        self.rawLogData = rawLogData
        self.validation = validation
