from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class UnitOfMeasure(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 unitOfMeasureTag: str = None):
        super().__init__()
        self.unitOfMeasureTag = unitOfMeasureTag
