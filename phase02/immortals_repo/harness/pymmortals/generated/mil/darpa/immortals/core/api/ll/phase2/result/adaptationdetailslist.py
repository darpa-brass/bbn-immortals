from typing import List


# noinspection PyPep8Naming
class AdaptationDetailsList(List):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 sequence: int = None):
        super().__init__()
        self.sequence = sequence


# noinspection PyPep8Naming
class AdaptationDetailsListDeserializer(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()


# noinspection PyPep8Naming
class AdaptationDetailsListSerializer(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
