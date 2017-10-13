from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class BytecodeVersion(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 majorVersionTag: str = None,
                 minorVersionTag: str = None,
                 platformVersionTag: str = None):
        super().__init__()
        self.majorVersionTag = majorVersionTag
        self.minorVersionTag = minorVersionTag
        self.platformVersionTag = platformVersionTag
