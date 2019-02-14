from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class Relation(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 name: str = None):
        super().__init__()
        self.name = name
