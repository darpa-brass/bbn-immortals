from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class EnableDas(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 dasEnabled: bool = None):
        super().__init__()
        self.dasEnabled = dasEnabled
