from enum import Enum


# noinspection PyPep8Naming
class ConstraintCriterionType(Enum):
    WHEN_HARD_VIOLATED = 'WHEN_HARD_VIOLATED'
    WHEN_SOFT_VIOLATED = 'WHEN_SOFT_VIOLATED'
    WHEN_WARNED = 'WHEN_WARNED'
