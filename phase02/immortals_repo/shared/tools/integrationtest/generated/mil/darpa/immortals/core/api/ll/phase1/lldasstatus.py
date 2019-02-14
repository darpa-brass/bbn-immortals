from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class LLDasStatus(Enum):
    def __init__(self, tag: str):
        self.tag: str = tag

    PERTURBATION_DETECTED = (
        "PERTURBATION_DETECTED")

    MISSION_SUSPENDED = (
        "MISSION_SUSPENDED")

    MISSION_RESUMED = (
        "MISSION_RESUMED")

    MISSION_HALTED = (
        "MISSION_HALTED")

    MISSION_ABORTED = (
        "MISSION_ABORTED")

    ADAPTING = (
        "ADAPTING")

    ADAPTATION_COMPLETED = (
        "ADAPTATION_COMPLETED")

    TEST_ERROR = (
        "TEST_ERROR")

    @classmethod
    def all_tag(cls) -> FrozenSet[str]:
        return frozenset([k.tag for k in list(cls)])