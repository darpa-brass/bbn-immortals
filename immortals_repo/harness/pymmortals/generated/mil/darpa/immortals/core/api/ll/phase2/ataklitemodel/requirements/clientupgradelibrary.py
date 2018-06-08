from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class ClientUpgradeLibrary(Enum):
    def __init__(self, key_idx_value: str, description: str, newDependencyCoordinates: str, oldDependencyCoordinates: str, repositoryUrl: str):
        self._key_idx_value = key_idx_value
        self.description = description  # type: str
        self.newDependencyCoordinates = newDependencyCoordinates  # type: str
        self.oldDependencyCoordinates = oldDependencyCoordinates  # type: str
        self.repositoryUrl = repositoryUrl  # type: str

    ToBeDetermined_X_X = ("ToBeDetermined_X_X",
        "Libraries to be determined",
        "dummy:new:version",
        "dummy:old:version",
        "http://central.maven.org/maven2/")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])

    @classmethod
    def all_newDependencyCoordinates(cls) -> FrozenSet[str]:
        return frozenset([k.newDependencyCoordinates for k in list(cls)])

    @classmethod
    def all_oldDependencyCoordinates(cls) -> FrozenSet[str]:
        return frozenset([k.oldDependencyCoordinates for k in list(cls)])

    @classmethod
    def all_repositoryUrl(cls) -> FrozenSet[str]:
        return frozenset([k.repositoryUrl for k in list(cls)])
