from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class JavaResource(Enum):
    def __init__(self, key_idx_value: str, description: str):
        self._key_idx_value = key_idx_value
        self.description = description  # type: str

    HARWARE_AES = ("HARWARE_AES",
        "Hardware accelerated AES cryptography")

    STRONG_CRYPTO = ("STRONG_CRYPTO",
        "Support for strong cryptography")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])
