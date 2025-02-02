from enum import Enum
from typing import FrozenSet
from typing import List


# noinspection PyPep8Naming
class ClientPartialUpgradeLibrary(Enum):
    def __init__(self, key_idx_value: str, description: str, newDependencyCoordinates: str, oldDependencyCoordinates: str, repositoryUrl: str, vulnerabilityIdentifiers: List[str]):
        self._key_idx_value = key_idx_value
        self.description = description  # type: str
        self.newDependencyCoordinates = newDependencyCoordinates  # type: str
        self.oldDependencyCoordinates = oldDependencyCoordinates  # type: str
        self.repositoryUrl = repositoryUrl  # type: str
        self.vulnerabilityIdentifiers = vulnerabilityIdentifiers  # type: List[str]

    Dropbox_3_0_6 = ("Dropbox_3_0_6",
        "Version of dropbox containing a resolution for a security flaw",
        "com.dropbox.core:dropbox-core-sdk:3.0.6",
        "com.dropbox.core:dropbox-core-sdk:3.0.3",
        "http://central.maven.org/maven2/",
        ["https://github.com/dropbox/dropbox-sdk-java/issues/78"])

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

    @classmethod
    def all_vulnerabilityIdentifiers(cls) -> FrozenSet[List[str]]:
        return frozenset([k.vulnerabilityIdentifiers for k in list(cls)])
