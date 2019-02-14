from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionality import Functionality
from pymmortals.generated.com.securboration.immortals.ontology.lang.compiledcodeunit import CompiledCodeUnit
from typing import Type


# noinspection PyPep8Naming
class ControlPoint(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 controlPointUuid: str = None,
                 functionalSignature: Type[Functionality] = None,
                 owner: CompiledCodeUnit = None):
        super().__init__()
        self.controlPointUuid = controlPointUuid
        self.functionalSignature = functionalSignature
        self.owner = owner
