from pymmortals.generated.com.securboration.immortals.ontology.core.humanreadable import HumanReadable


# noinspection PyPep8Naming
class InterProcessFunctionalityEntry(HumanReadable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDesc: str = None,
                 methodPointer: str = None):
        super().__init__()
        self.humanReadableDesc = humanReadableDesc
        self.methodPointer = methodPointer
