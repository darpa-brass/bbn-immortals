from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class Modifier(Enum):
    def __init__(self, access: int):
        self.access: int = access

    ABSTRACT = (
        1024)

    FINAL = (
        16)

    INTERFACE = (
        512)

    NATIVE = (
        256)

    PRIVATE = (
        2)

    PROTECTED = (
        4)

    PUBLIC = (
        1)

    STATIC = (
        8)

    STRICT = (
        2048)

    SYNCHRONIZED = (
        32)

    TRANSIENT = (
        128)

    VOLATILE = (
        64)

    ANNOTATION = (
        8192)

    BRIDGE = (
        64)

    DEPRECATED = (
        131072)

    ENUM = (
        16384)

    MANDATED = (
        32768)

    SUPER = (
        32)

    SYNTHETIC = (
        4096)

    VARARGS = (
        128)

    @classmethod
    def all_access(cls) -> FrozenSet[int]:
        return frozenset([k.access for k in list(cls)])