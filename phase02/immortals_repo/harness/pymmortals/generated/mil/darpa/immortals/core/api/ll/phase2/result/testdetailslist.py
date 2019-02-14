from typing import List


# noinspection PyPep8Naming
class TestDetailsList(List):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 sequence: int = None):
        super().__init__()
        self.sequence = sequence


# noinspection PyPep8Naming
class TestDetailsListDeserializer(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()


# noinspection PyPep8Naming
class TestDetailsListSerializer(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
