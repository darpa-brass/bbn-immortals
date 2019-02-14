from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.diskresource import DiskResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.memory.memoryresource import MemoryResource
from typing import List


# noinspection PyPep8Naming
class Partition(DiskResource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 memoryModel: MemoryResource = None,
                 resourceProperty: List[Property] = None):
        super().__init__(humanReadableDescription=humanReadableDescription, memoryModel=memoryModel, resourceProperty=resourceProperty)
