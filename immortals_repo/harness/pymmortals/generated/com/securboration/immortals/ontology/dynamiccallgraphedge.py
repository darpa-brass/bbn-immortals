from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class DynamicCallGraphEdge(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 calledHash: str = None,
                 callerHash: str = None,
                 note: str = None):
        super().__init__()
        self.calledHash = calledHash
        self.callerHash = callerHash
        self.note = note
