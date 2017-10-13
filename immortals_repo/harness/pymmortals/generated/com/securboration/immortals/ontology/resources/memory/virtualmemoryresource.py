from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.memory.memoryresource import MemoryResource
from typing import List


# noinspection PyPep8Naming
class VirtualMemoryResource(MemoryResource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 canRead: bool = None,
                 canWrite: bool = None,
                 humanReadableDescription: str = None,
                 maxAvailableBytes: int = None,
                 resourceProperty: List[Property] = None):
        super().__init__(canRead=canRead, canWrite=canWrite, humanReadableDescription=humanReadableDescription, maxAvailableBytes=maxAvailableBytes, resourceProperty=resourceProperty)
