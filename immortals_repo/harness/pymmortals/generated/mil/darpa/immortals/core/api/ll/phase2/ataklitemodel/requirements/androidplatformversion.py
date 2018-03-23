from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class AndroidPlatformVersion(Enum):
    def __init__(self, key_idx_value: str, description: str):
        self.key_idx_value = key_idx_value
        self.description = description  # type: str

    Android21 = ("Android21",
        "Baseline Android API version 21")

    Android23 = ("Android23",
        "Newer Android API version 23 which requires runtime permission requests")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])
