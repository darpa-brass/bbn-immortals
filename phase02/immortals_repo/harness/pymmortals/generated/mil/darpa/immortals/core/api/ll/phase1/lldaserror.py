from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class LLDasError(Enum):
    def __init__(self, tag: str):
        self.tag: str = tag

    TEST_DATA_FILE_ERROR = (
        "TEST_DATA_FILE_ERROR")

    TEST_DATA_FORMAT_ERROR = (
        "TEST_DATA_FORMAT_ERROR")

    DAS_LOG_FILE_ERROR = (
        "DAS_LOG_FILE_ERROR")

    DAS_OTHER_ERROR = (
        "DAS_OTHER_ERROR")

    @classmethod
    def all_tag(cls) -> FrozenSet[str]:
        return frozenset([k.tag for k in list(cls)])