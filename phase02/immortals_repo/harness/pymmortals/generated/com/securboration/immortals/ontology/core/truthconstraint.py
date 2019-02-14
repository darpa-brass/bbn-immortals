from enum import Enum


# noinspection PyPep8Naming
class TruthConstraint(Enum):
    NONE = 'NONE'
    NEVER_TRUE = 'NEVER_TRUE'
    USUALLY_NOT_TRUE = 'USUALLY_NOT_TRUE'
    SOMETIMES_TRUE = 'SOMETIMES_TRUE'
    USUALLY_TRUE = 'USUALLY_TRUE'
    ALWAYS_TRUE = 'ALWAYS_TRUE'
