from integrationtest.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class CastorConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 identifier: str = None):
        super().__init__()
        self.identifier = identifier
