from integrationtest.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class PostGreSqlConfig(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 enabled: bool = None,
                 serverName: str = None,
                 serverPassword: str = None,
                 serverPort: int = None,
                 serverUsername: str = None):
        super().__init__()
        self.enabled = enabled
        self.serverName = serverName
        self.serverPassword = serverPassword
        self.serverPort = serverPort
        self.serverUsername = serverUsername
