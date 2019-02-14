from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class TestHarnessConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 port: int = None,
                 protocol: str = None,
                 url: str = None):
        super().__init__()
        self.port = port
        self.protocol = protocol
        self.url = url
