from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class CodeUnit(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
