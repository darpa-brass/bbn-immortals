from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.platformresource import PlatformResource
from typing import List


# noinspection PyPep8Naming
class MemoryResource(PlatformResource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 canRead: bool = None,
                 canWrite: bool = None,
                 humanReadableDescription: str = None,
                 maxAvailableBytes: int = None,
                 resourceProperty: List[Property] = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.canRead = canRead
        self.canWrite = canWrite
        self.maxAvailableBytes = maxAvailableBytes
