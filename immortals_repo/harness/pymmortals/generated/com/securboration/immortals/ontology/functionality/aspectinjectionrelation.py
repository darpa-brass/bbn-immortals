from enum import Enum


# noinspection PyPep8Naming
class AspectInjectionRelation(Enum):
    JUST_BEFORE = 'JUST_BEFORE'
    JUST_AFTER = 'JUST_AFTER'
    DURING = 'DURING'
