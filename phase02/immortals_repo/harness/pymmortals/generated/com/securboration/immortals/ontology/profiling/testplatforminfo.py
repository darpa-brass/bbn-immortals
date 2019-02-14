from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class TestPlatformInfo(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 platformInfo: str = None):
        super().__init__()
        self.platformInfo = platformInfo
