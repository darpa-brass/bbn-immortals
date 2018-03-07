from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class ServerUpgradeLibrary(Enum):
    def __init__(self, description: str):
        self.description = description  # type: str

    ImageSaverLibrary_2 = (
        "Newer image saver library")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])
