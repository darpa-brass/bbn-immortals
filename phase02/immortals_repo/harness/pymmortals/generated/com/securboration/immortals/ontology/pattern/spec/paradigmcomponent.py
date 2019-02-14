from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class ParadigmComponent(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 durableId: str = None,
                 multiplicityOperator: str = None,
                 ordering: int = None):
        super().__init__()
        self.durableId = durableId
        self.multiplicityOperator = multiplicityOperator
        self.ordering = ordering
