from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class ClasspathElement(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 binaryForm: bytes = None,
                 hash: str = None,
                 name: str = None):
        super().__init__()
        self.binaryForm = binaryForm
        self.hash = hash
        self.name = name
