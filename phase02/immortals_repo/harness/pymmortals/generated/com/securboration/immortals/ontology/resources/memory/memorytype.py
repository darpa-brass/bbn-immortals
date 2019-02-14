from enum import Enum


# noinspection PyPep8Naming
class MemoryType(Enum):
    Cache_L1 = 'Cache_L1'
    Cache_L2 = 'Cache_L2'
    Cache_L3 = 'Cache_L3'
    DRAM = 'DRAM'
    SRAM = 'SRAM'
    ROM = 'ROM'
    SDRAM = 'SDRAM'
    PCM = 'PCM'
