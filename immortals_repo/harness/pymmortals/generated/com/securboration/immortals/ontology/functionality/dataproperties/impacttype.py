from enum import Enum


# noinspection PyPep8Naming
class ImpactType(Enum):
    IMPROVES_SIGNIFICANTLY = 'IMPROVES_SIGNIFICANTLY'
    IMPROVES = 'IMPROVES'
    DOES_NOT_AFFECT = 'DOES_NOT_AFFECT'
    DEGRADES = 'DEGRADES'
    DEGRADES_SIGNIFICANTLY = 'DEGRADES_SIGNIFICANTLY'
    OPTIMIZES = 'OPTIMIZES'
    REPAIRS = 'REPAIRS'
    DESTROYS = 'DESTROYS'
    INCREASES = 'INCREASES'
    DECREASES = 'DECREASES'
    MAXIMIZES = 'MAXIMIZES'
    MINIMIZES = 'MINIMIZES'
    ADDS = 'ADDS'
    REMOVES = 'REMOVES'
