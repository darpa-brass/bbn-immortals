from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class GlobalResource(Enum):
    def __init__(self, key_idx_value: str, description: str):
        self._key_idx_value = key_idx_value
        self.description = description  # type: str

    ToBeDetermined_X_X = ("ToBeDetermined_X_X",
        "Resources to be determined")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])
