from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataflow import DataFlow
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class Output(DataFlow):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 flowName: str = None,
                 output: 'Output' = None,
                 properties: List[Property] = None,
                 specTag: str = None,
                 type: Type[DataType] = None):
        super().__init__(flowName=flowName, properties=properties, specTag=specTag, type=type)
        self.output = output
