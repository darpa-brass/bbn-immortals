from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.computeresource import ComputeResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.memory.memoryresource import MemoryResource
from typing import List


# noinspection PyPep8Naming
class Gpu(ComputeResource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 memoryModel: MemoryResource = None,
                 resourceProperty: List[Property] = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.memoryModel = memoryModel
