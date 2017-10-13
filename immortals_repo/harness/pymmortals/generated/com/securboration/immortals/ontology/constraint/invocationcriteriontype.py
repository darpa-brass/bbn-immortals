from enum import Enum


# noinspection PyPep8Naming
class InvocationCriterionType(Enum):
    BEFORE_INVOKING = 'BEFORE_INVOKING'
    DURING_INVOCATION = 'DURING_INVOCATION'
    AFTER_INVOKING = 'AFTER_INVOKING'
