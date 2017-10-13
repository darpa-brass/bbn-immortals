from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.network.applicationlayerabstraction import ApplicationLayerAbstraction
from pymmortals.generated.com.securboration.immortals.ontology.resources.network.linklayerabstraction import LinkLayerAbstraction
from pymmortals.generated.com.securboration.immortals.ontology.resources.network.networklayerabstraction import NetworkLayerAbstraction
from pymmortals.generated.com.securboration.immortals.ontology.resources.network.physicallayerabstraction import PhysicalLayerAbstraction
from pymmortals.generated.com.securboration.immortals.ontology.resources.network.transportlayerabstraction import TransportLayerAbstraction
from pymmortals.generated.com.securboration.immortals.ontology.resources.networkresource import NetworkResource
from typing import List


# noinspection PyPep8Naming
class NetworkStackAbstraction(NetworkResource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 applicationLayer: ApplicationLayerAbstraction = None,
                 humanReadableDescription: str = None,
                 internetLayer: NetworkLayerAbstraction = None,
                 linkLayer: LinkLayerAbstraction = None,
                 physicalLayer: PhysicalLayerAbstraction = None,
                 resourceProperty: List[Property] = None,
                 transportLayer: TransportLayerAbstraction = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.applicationLayer = applicationLayer
        self.internetLayer = internetLayer
        self.linkLayer = linkLayer
        self.physicalLayer = physicalLayer
        self.transportLayer = transportLayer
