from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class AnalyticsEvent(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 data: str = None,
                 dataType: str = None,
                 eventId: int = None,
                 eventRemoteSource: str = None,
                 eventSource: str = None,
                 eventTime: int = None,
                 type: str = None):
        super().__init__()
        self.data = data
        self.dataType = dataType
        self.eventId = eventId
        self.eventRemoteSource = eventRemoteSource
        self.eventSource = eventSource
        self.eventTime = eventTime
        self.type = type
