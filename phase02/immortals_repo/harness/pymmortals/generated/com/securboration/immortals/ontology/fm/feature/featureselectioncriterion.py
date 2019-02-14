from enum import Enum


# noinspection PyPep8Naming
class FeatureSelectionCriterion(Enum):
    SELECT_ALL = 'SELECT_ALL'
    SELECT_ZERO_OR_MORE = 'SELECT_ZERO_OR_MORE'
    SELECT_ONE_OR_MORE = 'SELECT_ONE_OR_MORE'
    SELECT_EXACTLY_ONE = 'SELECT_EXACTLY_ONE'
