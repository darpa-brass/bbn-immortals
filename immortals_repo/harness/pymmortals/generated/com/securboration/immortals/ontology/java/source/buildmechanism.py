from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class BuildMechanism(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 buildMechanism: str = None):
        super().__init__()
        self.buildMechanism = buildMechanism
