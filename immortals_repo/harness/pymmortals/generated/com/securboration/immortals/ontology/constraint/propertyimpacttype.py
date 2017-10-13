from enum import Enum


# noinspection PyPep8Naming
class PropertyImpactType(Enum):
    ADDS = 'ADDS'
    REMOVES = 'REMOVES'
    PROPERTY_INCREASES = 'PROPERTY_INCREASES'
    PROPERTY_DECREASES = 'PROPERTY_DECREASES'
    DOES_NOT_AFFECT = 'DOES_NOT_AFFECT'
