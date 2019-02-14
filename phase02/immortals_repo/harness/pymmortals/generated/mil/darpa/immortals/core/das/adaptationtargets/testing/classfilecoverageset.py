from typing import Set


# noinspection PyPep8Naming
class ClassFileCoverageSet(Set):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
