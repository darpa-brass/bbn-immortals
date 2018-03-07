from pymmortals.generated.com.securboration.immortals.ontology.analysis.dataflowanalysisframe import DataflowAnalysisFrame
from pymmortals.generated.com.securboration.immortals.ontology.analysis.dataflowedge import DataflowEdge
from pymmortals.generated.com.securboration.immortals.ontology.analysis.dataflownode import DataflowNode
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class InterMethodDataflowEdge(DataflowEdge):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 communicationChannelTemplate: Resource = None,
                 consumer: DataflowNode = None,
                 dataTypeCommunicated: Type[DataType] = None,
                 dataflowAnalysisFrame: DataflowAnalysisFrame = None,
                 edgeProperties: List[Property] = None,
                 humanReadableDescription: str = None,
                 producer: DataflowNode = None):
        super().__init__(communicationChannelTemplate=communicationChannelTemplate, consumer=consumer, dataTypeCommunicated=dataTypeCommunicated, dataflowAnalysisFrame=dataflowAnalysisFrame, edgeProperties=edgeProperties, humanReadableDescription=humanReadableDescription, producer=producer)
