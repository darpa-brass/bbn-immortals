from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.ioresource import IOResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.partition import Partition
from typing import List


# noinspection PyPep8Naming
class PartitionedDiskResource(IOResource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 partitions: List[Partition] = None,
                 resourceProperty: List[Property] = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.partitions = partitions
