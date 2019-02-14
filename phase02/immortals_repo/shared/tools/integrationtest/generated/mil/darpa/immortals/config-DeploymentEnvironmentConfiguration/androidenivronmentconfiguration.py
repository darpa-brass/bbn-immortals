from integrationtest.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class AndroidEnivronmentConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 adbIdentifier: str = None,
                 adbPort: int = None,
                 adbUrl: str = None,
                 androidVersion: int = None):
        super().__init__()
        self.adbIdentifier = adbIdentifier
        self.adbPort = adbPort
        self.adbUrl = adbUrl
        self.androidVersion = androidVersion
