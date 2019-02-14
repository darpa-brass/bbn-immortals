from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.diskresource import DiskResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.memory.memoryresource import MemoryResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.partition import Partition
from typing import List


# noinspection PyPep8Naming
class Volume(DiskResource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 memoryModel: MemoryResource = None,
                 partitionsSpanned: List[Partition] = None,
                 resourceProperty: List[Property] = None):
        super().__init__(humanReadableDescription=humanReadableDescription, memoryModel=memoryModel, resourceProperty=resourceProperty)
        self.partitionsSpanned = partitionsSpanned
