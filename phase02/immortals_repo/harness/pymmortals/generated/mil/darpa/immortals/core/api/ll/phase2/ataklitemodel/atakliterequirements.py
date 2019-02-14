from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.androidplatformversion import AndroidPlatformVersion
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientpartialupgradelibrary import ClientPartialUpgradeLibrary
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientupgradelibrary import ClientUpgradeLibrary


# noinspection PyPep8Naming
class AtakliteRequirements(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 deploymentPlatformVersion: AndroidPlatformVersion = None,
                 libraryUpgrade: ClientUpgradeLibrary = None,
                 partialLibraryUpgrade: ClientPartialUpgradeLibrary = None):
        super().__init__()
        self.deploymentPlatformVersion = deploymentPlatformVersion
        self.libraryUpgrade = libraryUpgrade
        self.partialLibraryUpgrade = partialLibraryUpgrade
