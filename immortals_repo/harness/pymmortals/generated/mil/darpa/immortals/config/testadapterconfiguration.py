from pymmortals.datatypes.serializable import Serializable
from typing import Dict
from typing import List


# noinspection PyPep8Naming
class TestAdapterConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 environmentVariables: Dict[str,str] = None,
                 exePath: str = None,
                 identifier: str = None,
                 interpreterParameters: List[str] = None,
                 parameters: List[str] = None,
                 port: int = None,
                 protocol: str = None,
                 readyStdoutLineRegexPattern: str = None,
                 startupTimeMS: int = None,
                 url: str = None,
                 userManaged: bool = None,
                 websocketPort: int = None,
                 workingDirectory: str = None):
        super().__init__()
        self.environmentVariables = environmentVariables
        self.exePath = exePath
        self.identifier = identifier
        self.interpreterParameters = interpreterParameters
        self.parameters = parameters
        self.port = port
        self.protocol = protocol
        self.readyStdoutLineRegexPattern = readyStdoutLineRegexPattern
        self.startupTimeMS = startupTimeMS
        self.url = url
        self.userManaged = userManaged
        self.websocketPort = websocketPort
        self.workingDirectory = workingDirectory
