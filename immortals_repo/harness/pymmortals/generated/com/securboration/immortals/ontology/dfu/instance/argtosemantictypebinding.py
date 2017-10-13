from pymmortals.generated.com.securboration.immortals.ontology.dfu.instance.flowtosemantictypebinding import FlowToSemanticTypeBinding
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class ArgToSemanticTypeBinding(FlowToSemanticTypeBinding):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 argIndex: int = None,
                 comment: str = None,
                 properties: List[Property] = None,
                 semanticType: Type[DataType] = None):
        super().__init__(argIndex=argIndex, comment=comment, properties=properties, semanticType=semanticType)
