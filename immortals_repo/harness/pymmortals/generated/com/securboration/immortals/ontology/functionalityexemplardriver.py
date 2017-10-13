from pymmortals.datatypes.serializable import Serializable
from typing import List


# noinspection PyPep8Naming
class FunctionalityExemplarDriver(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 instructions: List[str] = None,
                 note: str = None,
                 templateTaught: str = None):
        super().__init__()
        self.instructions = instructions
        self.note = note
        self.templateTaught = templateTaught
