from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.analysis.dataflowanalysisframe import DataflowAnalysisFrame
from pymmortals.generated.com.securboration.immortals.ontology.analysis.dataflownode import DataflowNode
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class DataflowEdge(Serializable):
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
        super().__init__()
        self.communicationChannelTemplate = communicationChannelTemplate
        self.consumer = consumer
        self.dataTypeCommunicated = dataTypeCommunicated
        self.dataflowAnalysisFrame = dataflowAnalysisFrame
        self.edgeProperties = edgeProperties
        self.humanReadableDescription = humanReadableDescription
        self.producer = producer
