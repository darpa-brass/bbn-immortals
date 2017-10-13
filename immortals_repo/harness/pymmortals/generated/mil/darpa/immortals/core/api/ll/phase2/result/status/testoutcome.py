from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class TestOutcome(Enum):
    def __init__(self, description: str):
        self.description: str = description

    PENDING = (
        "Indicates the specified action is pending (non-terminal)")

    RUNNING = (
        "Indicates the specified action is running (non-terminal")

    NOT_APPLICABLE = (
        "Indicates the specified action is not applicable")

    COMPLETE = (
        "See LL Evaluation Methodology")

    INVALID = (
        "See LL Evaluation Methodology")

    INCOMPLETE = (
        "See LL Evaluation Methodology")

    ERROR = (
        "See LL Evaluation Methodology")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])