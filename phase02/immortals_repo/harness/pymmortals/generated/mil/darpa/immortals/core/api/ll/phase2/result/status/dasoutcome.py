from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class DasOutcome(Enum):
    def __init__(self, key_idx_value: str, description: str):
        self._key_idx_value = key_idx_value
        self.description = description  # type: str

    PENDING = ("PENDING",
        "DAS execution is pending (non-terminal)")

    RUNNING = ("RUNNING",
        "DAS is executing analysis and adaptation (non-terminal)")

    NOT_APPLICABLE = ("NOT_APPLICABLE",
        "Baseline Submission - No DAS needed")

    NOT_POSSIBLE = ("NOT_POSSIBLE",
        "An invalid perturbation has been submitted")

    SUCCESS = ("SUCCESS",
        "Adaptation Successful")

    FAIL = ("FAIL",
        "Adaptation attempted but failed adaptation module validation")

    ERROR = ("ERROR",
        "An unexpected error has occured")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])
