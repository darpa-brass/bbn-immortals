from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.compute.cpu import Cpu
from pymmortals.generated.com.securboration.immortals.ontology.resources.compute.gpu import Gpu
from pymmortals.generated.com.securboration.immortals.ontology.resources.diskresource import DiskResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.executionplatform import ExecutionPlatform
from pymmortals.generated.com.securboration.immortals.ontology.resources.logical.logicalresource import LogicalResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.logical.operatingsystem import OperatingSystem
from pymmortals.generated.com.securboration.immortals.ontology.resources.memory.physicalmemoryresource import PhysicalMemoryResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.network.networkinterface import NetworkInterface
from pymmortals.generated.com.securboration.immortals.ontology.resources.platformresource import PlatformResource
from typing import List


# noinspection PyPep8Naming
class AndroidPlatform(ExecutionPlatform):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 androidPlatformVersion: str = None,
                 cpus: List[Cpu] = None,
                 deviceMemory: PhysicalMemoryResource = None,
                 disks: List[DiskResource] = None,
                 gpus: List[Gpu] = None,
                 humanReadableDescription: str = None,
                 networkInterfaces: List[NetworkInterface] = None,
                 os: OperatingSystem = None,
                 platformLibraries: List[LogicalResource] = None,
                 platformResources: List[PlatformResource] = None,
                 resourceProperty: List[Property] = None):
        super().__init__(cpus=cpus, deviceMemory=deviceMemory, disks=disks, gpus=gpus, humanReadableDescription=humanReadableDescription, networkInterfaces=networkInterfaces, os=os, platformLibraries=platformLibraries, resourceProperty=resourceProperty)
        self.androidPlatformVersion = androidPlatformVersion
        self.platformResources = platformResources
