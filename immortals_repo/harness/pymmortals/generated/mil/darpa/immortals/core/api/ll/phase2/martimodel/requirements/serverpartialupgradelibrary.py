from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class ServerPartialUpgradeLibrary(Enum):
    def __init__(self, description: str):
        self.description = description  # type: str

    Dom4jCot_2 = (
        "Newer cot processing library")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])
