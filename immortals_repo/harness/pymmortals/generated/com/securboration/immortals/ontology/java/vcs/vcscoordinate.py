from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class VcsCoordinate(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 version: str = None,
                 versionControlUrl: str = None):
        super().__init__()
        self.version = version
        self.versionControlUrl = versionControlUrl
