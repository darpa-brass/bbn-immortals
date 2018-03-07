from pymmortals.datatypes.serializable import Serializable
from typing import List


# noinspection PyPep8Naming
class DeploymentEnvironmentConfiguration(Serializable):

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

    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 androidEnvironments: List[AndroidEnivronmentConfiguration] = None,
                 martiAddress: str = None):
        super().__init__()
        self.androidEnvironments = androidEnvironments
        self.martiAddress = martiAddress
