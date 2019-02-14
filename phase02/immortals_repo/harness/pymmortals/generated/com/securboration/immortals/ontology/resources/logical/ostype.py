from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class OsType(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 osName: str = None,
                 osVersionTag: str = None):
        super().__init__()
        self.osName = osName
        self.osVersionTag = osVersionTag
