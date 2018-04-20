from typing import Set


# noinspection PyPep8Naming
class TestCaseReportSet(Set):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
