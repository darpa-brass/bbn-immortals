from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class SecurityStandard(Enum):
    def __init__(self, key_idx_value: str, algorithm: str, cipherChainingMode: str, description: str, keySize: int):
        self._key_idx_value = key_idx_value
        self.algorithm = algorithm  # type: str
        self.cipherChainingMode = cipherChainingMode  # type: str
        self.description = description  # type: str
        self.keySize = keySize  # type: int

    AES_128 = ("AES_128",
        "AES",
        None,
        "AES encryption algorithm with 128 bit key",
        16)

    AES_256 = ("AES_256",
        "AES",
        None,
        "AES encryption algorithm with 256 bit key",
        32)

    DES_56 = ("DES_56",
        "DES",
        None,
        "DES encryption algorithm with 56 bit key",
        8)

    Rijndael = ("Rijndael",
        "Rijndael",
        None,
        "Rijndael encryption algorithm",
        None)

    @classmethod
    def all_algorithm(cls) -> FrozenSet[str]:
        return frozenset([k.algorithm for k in list(cls)])

    @classmethod
    def all_cipherChainingMode(cls) -> FrozenSet[str]:
        return frozenset([k.cipherChainingMode for k in list(cls)])

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])

    @classmethod
    def all_keySize(cls) -> FrozenSet[int]:
        return frozenset([k.keySize for k in list(cls)])
