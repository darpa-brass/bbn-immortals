from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class ServerPartialUpgradeLibrary(Enum):
    def __init__(self, description: str, latestVersion: str):
        self.description: str = description
        self.latestVersion: str = latestVersion

    Dom4jCot = (
        "Dom4J library used for decoding incoming XML data into CoT objects",
        "2")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])

    @classmethod
    def all_latestVersion(cls) -> FrozenSet[str]:
        return frozenset([k.latestVersion for k in list(cls)])