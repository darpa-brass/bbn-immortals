from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.executionplatform import ExecutionPlatform
from pymmortals.generated.com.securboration.immortals.ontology.resources.ioresource import IOResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.network.networkstackabstraction import NetworkStackAbstraction
from typing import List


# noinspection PyPep8Naming
class NetworkConnection(IOResource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 localDevice: ExecutionPlatform = None,
                 network: NetworkStackAbstraction = None,
                 oneWay: bool = None,
                 remoteDevice: ExecutionPlatform = None,
                 resourceProperty: List[Property] = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.localDevice = localDevice
        self.network = network
        self.oneWay = oneWay
        self.remoteDevice = remoteDevice
