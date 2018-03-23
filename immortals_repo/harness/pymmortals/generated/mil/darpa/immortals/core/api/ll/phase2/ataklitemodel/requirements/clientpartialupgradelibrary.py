from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class ClientPartialUpgradeLibrary(Enum):
    def __init__(self, key_idx_value: str, description: str):
        self.key_idx_value = key_idx_value
        self.description = description  # type: str

    Dropbox_X_X = ("Dropbox_X_X",
        "Version of dropbox containing a resolution for a security flaw")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])
