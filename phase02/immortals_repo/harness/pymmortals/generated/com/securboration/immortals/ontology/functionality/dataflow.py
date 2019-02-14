from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class DataFlow(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 flowName: str = None,
                 properties: List[Property] = None,
                 specTag: str = None,
                 type: Type[DataType] = None):
        super().__init__()
        self.flowName = flowName
        self.properties = properties
        self.specTag = specTag
        self.type = type
