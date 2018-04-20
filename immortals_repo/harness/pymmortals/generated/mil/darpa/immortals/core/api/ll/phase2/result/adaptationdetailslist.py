from typing import Set


# noinspection PyPep8Naming
class AdaptationDetailsList(Set):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 timestamp: int = None):
        super().__init__()
        self.timestamp = timestamp
