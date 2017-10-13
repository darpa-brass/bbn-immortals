from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class InvocationType(Enum):
    def __init__(self, type: int):
        self.type: int = type

    INTERFACE = (
        185)

    SPECIAL = (
        183)

    STATIC = (
        184)

    VIRTUAL = (
        182)

    @classmethod
    def all_type(cls) -> FrozenSet[int]:
        return frozenset([k.type for k in list(cls)])