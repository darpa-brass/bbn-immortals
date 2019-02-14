from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class AbstractGsonHelper(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()


# noinspection PyPep8Naming
class FileDeserializer(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()


# noinspection PyPep8Naming
class PathDeserializer(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
