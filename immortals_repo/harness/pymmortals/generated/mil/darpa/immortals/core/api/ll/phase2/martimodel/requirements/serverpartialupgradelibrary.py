from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class ServerPartialUpgradeLibrary(Enum):
    def __init__(self, key_idx_value: str, description: str, newDependencyCoordinates: str, oldDependencyCoordinates: str):
        self._key_idx_value = key_idx_value
        self.description = description  # type: str
        self.newDependencyCoordinates = newDependencyCoordinates  # type: str
        self.oldDependencyCoordinates = oldDependencyCoordinates  # type: str

    Dom4jCot_2 = ("Dom4jCot_2",
        "Newer cot processing library",
        "NewLib",
        "OldLib")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])

    @classmethod
    def all_newDependencyCoordinates(cls) -> FrozenSet[str]:
        return frozenset([k.newDependencyCoordinates for k in list(cls)])

    @classmethod
    def all_oldDependencyCoordinates(cls) -> FrozenSet[str]:
        return frozenset([k.oldDependencyCoordinates for k in list(cls)])
