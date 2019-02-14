from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class Value(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 stringValue: str = None):
        super().__init__()
        self.stringValue = stringValue
