from enum import Enum
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.resttype import RestType
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.testadapterstate import TestAdapterState
from typing import FrozenSet
from typing import Type


# noinspection PyPep8Naming
class TestHarnessEndpoint(Enum):
    def __init__(self, ackDatatype: Type, path: str, restType: RestType, submitDatatype: Type):
        self.ackDatatype: Type = ackDatatype
        self.path: str = path
        self.restType: RestType = restType
        self.submitDatatype: Type = submitDatatype

    READY = (
        None,
        "/ready",
        RestType.POST,
        None)

    ERROR = (
        None,
        "/error",
        RestType.POST,
        str)

    STATUS = (
        None,
        "/status",
        RestType.POST,
        TestAdapterState)

    DONE = (
        None,
        "/done",
        RestType.POST,
        TestAdapterState)

    @classmethod
    def all_ackDatatype(cls) -> FrozenSet[Type]:
        return frozenset([k.ackDatatype for k in list(cls)])

    @classmethod
    def all_path(cls) -> FrozenSet[str]:
        return frozenset([k.path for k in list(cls)])

    @classmethod
    def all_restType(cls) -> FrozenSet[RestType]:
        return frozenset([k.restType for k in list(cls)])

    @classmethod
    def all_submitDatatype(cls) -> FrozenSet[Type]:
        return frozenset([k.submitDatatype for k in list(cls)])