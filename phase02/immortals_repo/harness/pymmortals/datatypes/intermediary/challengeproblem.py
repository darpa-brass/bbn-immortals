from enum import Enum

# noinspection PyPep8Naming
from typing import FrozenSet


class ChallengeProblem(Enum):
    def __init__(self, identifier: str):
        self.identifier: str = identifier

    Phase01 = (
        "p01cp1cp2")

    Phase02CP1 = (
        "p02cp1")

    Phase01CP2 = (
        "p02cp2")

    Phase01CP3 = (
        "p02cp3")

    @classmethod
    def all_identifier(cls) -> FrozenSet[str]:
        return frozenset([k.identifier for k in list(cls)])
