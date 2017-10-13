from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class DasOutcome(Enum):
    def __init__(self, description: str):
        self.description: str = description

    PENDING = (
        "DAS execution is pending (non-terminal)")

    RUNNING = (
        "DAS is executing analysis and augmentation (non-terminal)")

    NOT_APPLICABLE = (
        "Baseline Submission - No DAS needed")

    NOT_POSSIBLE = (
        "An invalid perturbation has been submitted")

    SUCCESS = (
        "Augmentation Successful")

    ERROR = (
        "An error has occured")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])