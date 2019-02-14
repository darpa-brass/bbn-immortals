from enum import Enum


# noinspection PyPep8Naming
class AnalyticsTarget(Enum):
    DEFAULT = 'DEFAULT'
    STDOUT = 'STDOUT'
    NET_LOG4J = 'NET_LOG4J'
    LOCAL_JSON_CONSUMER = 'LOCAL_JSON_CONSUMER'
