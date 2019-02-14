from enum import Enum


# noinspection PyPep8Naming
class Status(Enum):
    PENDING = 'PENDING'
    RUNNING = 'RUNNING'
    NOT_APPLICABLE = 'NOT_APPLICABLE'
    FAILURE = 'FAILURE'
    SUCCESS = 'SUCCESS'
