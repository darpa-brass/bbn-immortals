from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class CodeUnitPointer(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 className: str = None,
                 methodName: str = None,
                 pointerString: str = None):
        super().__init__()
        self.className = className
        self.methodName = methodName
        self.pointerString = pointerString
