from enum import Enum
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.resttype import RestType
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.testadapterstate import TestAdapterState
from typing import FrozenSet
from typing import Type


# noinspection PyPep8Naming
class TestHarnessEndpoint(Enum):
    def __init__(self, key_idx_value: str, ackDatatype: Type, path: str, restType: RestType, submitDatatype: Type):
        self._key_idx_value = key_idx_value
        self.ackDatatype = ackDatatype  # type: Type
        self.path = path  # type: str
        self.restType = restType  # type: RestType
        self.submitDatatype = submitDatatype  # type: Type

    READY = ("READY",
        None,
        "/ready",
        RestType.POST,
        None)

    ERROR = ("ERROR",
        None,
        "/error",
        RestType.POST,
        str)

    STATUS = ("STATUS",
        None,
        "/status",
        RestType.POST,
        TestAdapterState)

    DONE = ("DONE",
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
