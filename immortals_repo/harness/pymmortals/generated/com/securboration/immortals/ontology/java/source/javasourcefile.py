from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class JavaSourceFile(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 bytes: bytes = None,
                 name: str = None):
        super().__init__()
        self.bytes = bytes
        self.name = name
