from enum import Enum


# noinspection PyPep8Naming
class AlgorithmPurpose(Enum):
    UNDEFINED = 'UNDEFINED'
    SORTING = 'SORTING'
    COMPRESSION = 'COMPRESSION'
    ENCRYPTION = 'ENCRYPTION'
    ESTABLISH_SHARED_SECRET = 'ESTABLISH_SHARED_SECRET'
