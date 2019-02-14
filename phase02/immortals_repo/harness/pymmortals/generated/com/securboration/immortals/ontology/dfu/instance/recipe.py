from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class Recipe(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 recipe: str = None):
        super().__init__()
        self.recipe = recipe
