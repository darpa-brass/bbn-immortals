from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.memory.memoryresource import MemoryResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.memory.memorytype import MemoryType
from typing import List


# noinspection PyPep8Naming
class PhysicalMemoryResource(MemoryResource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 canRead: bool = None,
                 canWrite: bool = None,
                 humanReadableDescription: str = None,
                 maxAvailableBytes: int = None,
                 memoryType: MemoryType = None,
                 resourceProperty: List[Property] = None):
        super().__init__(canRead=canRead, canWrite=canWrite, humanReadableDescription=humanReadableDescription, maxAvailableBytes=maxAvailableBytes, resourceProperty=resourceProperty)
        self.memoryType = memoryType
