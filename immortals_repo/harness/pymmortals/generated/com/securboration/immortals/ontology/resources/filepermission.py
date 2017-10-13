from enum import Enum


# noinspection PyPep8Naming
class FilePermission(Enum):
    READ = 'READ'
    WRITE = 'WRITE'
    EXECUTE = 'EXECUTE'
