from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class VerdictOutcome(Enum):
    def __init__(self, description: str):
        self.description: str = description

    PENDING = (
        "The verdict outcome is pending")

    RUNNING = (
        "The validation is running")

    PASS = (
        "See LL Evaluation Methodology")

    DEGRADED = (
        "See LL Evaluation Methodology")

    FAIL = (
        "See LL Evaluation Methodology")

    INCONCLUSIVE = (
        "See LL Evaluation Methodology")

    INAPPLICABLE = (
        "See LL Evaluation Methodology")

    ERROR = (
        "See LL Evaluation Methodology")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])