from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.filesystemresource import FileSystemResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.logical.logicalresource import LogicalResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.partitioneddiskresource import PartitionedDiskResource
from typing import List


# noinspection PyPep8Naming
class OperatingSystem(LogicalResource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 fileSystem: FileSystemResource = None,
                 humanReadableDescription: str = None,
                 osPartition: PartitionedDiskResource = None,
                 osType: str = None,
                 resourceProperty: List[Property] = None,
                 systemLibraries: List[LogicalResource] = None,
                 versionTag: str = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.fileSystem = fileSystem
        self.osPartition = osPartition
        self.osType = osType
        self.systemLibraries = systemLibraries
        self.versionTag = versionTag
