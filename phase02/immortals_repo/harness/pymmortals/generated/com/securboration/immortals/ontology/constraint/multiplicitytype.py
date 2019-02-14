from enum import Enum


# noinspection PyPep8Naming
class MultiplicityType(Enum):
    APPLICABLE_TO_ONE_OF = 'APPLICABLE_TO_ONE_OF'
    APPLICABLE_TO_ALL_OF = 'APPLICABLE_TO_ALL_OF'
    APPLICABLE_TO_NONE_OF = 'APPLICABLE_TO_NONE_OF'
