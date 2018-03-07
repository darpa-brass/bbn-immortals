from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class ClientPartialUpgradeLibrary(Enum):
    def __init__(self, description: str):
        self.description = description  # type: str

    Dropbox_X_X = (
        "Version of dropbox containing a resolution for a security flaw")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])
