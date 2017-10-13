from enum import Enum
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.enabledas import EnableDas
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.resttype import RestType
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.testadapterstate import TestAdapterState
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.submissionmodel import SubmissionModel
from typing import FrozenSet
from typing import Type


# noinspection PyPep8Naming
class TestAdapterEndpoint(Enum):
    def __init__(self, ackDatatype: Type, path: str, restType: RestType, submitDatatype: Type):
        self.ackDatatype: Type = ackDatatype
        self.path: str = path
        self.restType: RestType = restType
        self.submitDatatype: Type = submitDatatype

    CP1 = (
        TestAdapterState,
        "/action/databaseSchemaPerturbation",
        RestType.POST,
        SubmissionModel)

    CP2 = (
        TestAdapterState,
        "/action/crossApplicationDependencies",
        RestType.POST,
        SubmissionModel)

    CP3 = (
        TestAdapterState,
        "/action/libraryEvolution",
        RestType.POST,
        SubmissionModel)

    ALIVE = (
        None,
        "/alive",
        RestType.GET,
        None)

    QUERY = (
        TestAdapterState,
        "/query",
        RestType.GET,
        None)

    ENABLED = (
        None,
        "/enabled",
        RestType.POST,
        EnableDas)

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