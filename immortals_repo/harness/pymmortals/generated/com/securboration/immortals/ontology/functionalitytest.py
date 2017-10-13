from pymmortals.generated.com.securboration.immortals.ontology.core.humanreadable import HumanReadable
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionality import Functionality
from typing import Type


# noinspection PyPep8Naming
class FunctionalityTest(HumanReadable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 functionality: Type[Functionality] = None,
                 humanReadableDesc: str = None):
        super().__init__()
        self.functionality = functionality
        self.humanReadableDesc = humanReadableDesc
