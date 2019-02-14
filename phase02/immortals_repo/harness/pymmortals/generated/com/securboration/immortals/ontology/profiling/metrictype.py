from enum import Enum


# noinspection PyPep8Naming
class MetricType(Enum):
    CPU_CYCLES_WALL = 'CPU_CYCLES_WALL'
    CPU_CYCLES_USER = 'CPU_CYCLES_USER'
    CPU_CYCLES_SYSTEM = 'CPU_CYCLES_SYSTEM'
    BYTES_READ = 'BYTES_READ'
    BYTES_WRITTEN = 'BYTES_WRITTEN'
