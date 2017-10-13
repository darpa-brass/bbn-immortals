from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class ClientUpgradeLibrary(Enum):
    def __init__(self, description: str, latestVersion: str):
        self.description: str = description
        self.latestVersion: str = latestVersion

    ToBeDetermined = (
        "A yet-to-be determined library",
        "1.3.3.7")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])

    @classmethod
    def all_latestVersion(cls) -> FrozenSet[str]:
        return frozenset([k.latestVersion for k in list(cls)])