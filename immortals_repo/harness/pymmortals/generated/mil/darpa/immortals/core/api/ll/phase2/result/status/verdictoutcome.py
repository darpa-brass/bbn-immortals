from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class VerdictOutcome(Enum):
    def __init__(self, key_idx_value: str, description: str):
        self._key_idx_value = key_idx_value
        self.description = description  # type: str

    PENDING = ("PENDING",
        "The verdict outcome is pending")

    RUNNING = ("RUNNING",
        "The validation is running")

    PASS = ("PASS",
        "See LL Evaluation Methodology")

    DEGRADED = ("DEGRADED",
        "See LL Evaluation Methodology")

    FAIL = ("FAIL",
        "See LL Evaluation Methodology")

    INCONCLUSIVE = ("INCONCLUSIVE",
        "See LL Evaluation Methodology")

    INAPPLICABLE = ("INAPPLICABLE",
        "See LL Evaluation Methodology")

    ERROR = ("ERROR",
        "See LL Evaluation Methodology")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])
