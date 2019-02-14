from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class AssertionBindingSite(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None):
        super().__init__()
        self.humanReadableDescription = humanReadableDescription
