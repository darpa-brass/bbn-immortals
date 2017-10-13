from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class DataflowAnalysisFrame(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 analysisFrameChild: 'DataflowAnalysisFrame' = None,
                 analysisFrameDataType: Type[DataType] = None,
                 frameProperties: List[Property] = None):
        super().__init__()
        self.analysisFrameChild = analysisFrameChild
        self.analysisFrameDataType = analysisFrameDataType
        self.frameProperties = frameProperties
