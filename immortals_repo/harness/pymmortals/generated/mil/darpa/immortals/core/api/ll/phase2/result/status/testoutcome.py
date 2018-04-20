from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class TestOutcome(Enum):
    def __init__(self, key_idx_value: str, description: str):
        self._key_idx_value = key_idx_value
        self.description = description  # type: str

    PENDING = ("PENDING",
        "Indicates the specified action is pending (non-terminal)")

    RUNNING = ("RUNNING",
        "Indicates the specified action is running (non-terminal")

    NOT_APPLICABLE = ("NOT_APPLICABLE",
        "Indicates the specified action is not applicable")

    COMPLETE_PASS = ("COMPLETE_PASS",
        "Indicates the test completed with a 'PASS'")

    COMPLETE_DEGRADED = ("COMPLETE_DEGRADED",
        "Indicates the test completed with a 'DEGRADED'")

    COMPLETE_FAIL = ("COMPLETE_FAIL",
        "Indicates the test completed with a 'FAIL'")

    INVALID = ("INVALID",
        "See LL Evaluation Methodology")

    INCOMPLETE = ("INCOMPLETE",
        "See LL Evaluation Methodology")

    ERROR = ("ERROR",
        "See LL Evaluation Methodology")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])
