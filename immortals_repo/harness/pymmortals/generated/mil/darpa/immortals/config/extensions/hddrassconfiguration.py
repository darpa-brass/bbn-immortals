from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class HddRassConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 gradleBuildFile: str = None,
                 identifier: str = None,
                 jarPath: str = None):
        super().__init__()
        self.gradleBuildFile = gradleBuildFile
        self.identifier = identifier
        self.jarPath = jarPath
