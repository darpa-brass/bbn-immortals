from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class CriterionStatement(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None):
        super().__init__()
        self.humanReadableDescription = humanReadableDescription
