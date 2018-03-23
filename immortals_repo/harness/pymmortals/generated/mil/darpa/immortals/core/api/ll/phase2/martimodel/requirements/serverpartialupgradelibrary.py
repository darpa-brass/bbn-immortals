from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class ServerPartialUpgradeLibrary(Enum):
    def __init__(self, key_idx_value: str, description: str):
        self.key_idx_value = key_idx_value
        self.description = description  # type: str

    Dom4jCot_2 = ("Dom4jCot_2",
        "Newer cot processing library")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])
