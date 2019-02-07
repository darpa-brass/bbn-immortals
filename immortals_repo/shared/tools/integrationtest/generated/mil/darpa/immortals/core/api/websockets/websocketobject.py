from integrationtest.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class WebsocketObject(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 data: str = None,
                 endpoint: str = None):
        super().__init__()
        self.data = data
        self.endpoint = endpoint
