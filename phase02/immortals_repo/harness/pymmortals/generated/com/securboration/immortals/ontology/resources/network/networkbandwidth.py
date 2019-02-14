from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.networkresource import NetworkResource
from typing import List


# noinspection PyPep8Naming
class NetworkBandwidth(NetworkResource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 resourceProperty: List[Property] = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
