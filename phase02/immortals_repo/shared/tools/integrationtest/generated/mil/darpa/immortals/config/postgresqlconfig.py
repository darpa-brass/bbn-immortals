from integrationtest.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class PostgresqlConfig(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
