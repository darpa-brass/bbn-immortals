from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class FlowToSemanticTypeBinding(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 argIndex: int = None,
                 comment: str = None,
                 properties: List[Property] = None,
                 semanticType: Type[DataType] = None):
        super().__init__()
        self.argIndex = argIndex
        self.comment = comment
        self.properties = properties
        self.semanticType = semanticType
