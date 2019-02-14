from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class FeatureRequirement(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
