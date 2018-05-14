from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class ServerUpgradeLibrary(Enum):
    def __init__(self, key_idx_value: str, description: str, newDependencyCoordinates: str, oldDependencyCoordinates: str):
        self._key_idx_value = key_idx_value
        self.description = description  # type: str
        self.newDependencyCoordinates = newDependencyCoordinates  # type: str
        self.oldDependencyCoordinates = oldDependencyCoordinates  # type: str

    ElevationApi_2 = ("ElevationApi_2",
        "Elevation API that provides security fixes and improved accuracy but requires a network connection",
        "mil.darpa.immortals.dfus:ElevationApi-2:2.0-LOCAL",
        "mil.darpa.immortals.dfus:ElevationApi-1:2.0-LOCAL")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])

    @classmethod
    def all_newDependencyCoordinates(cls) -> FrozenSet[str]:
        return frozenset([k.newDependencyCoordinates for k in list(cls)])

    @classmethod
    def all_oldDependencyCoordinates(cls) -> FrozenSet[str]:
        return frozenset([k.oldDependencyCoordinates for k in list(cls)])
