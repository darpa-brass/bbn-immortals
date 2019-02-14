from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.androidplatformversion import AndroidPlatformVersion
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientpartialupgradelibrary import ClientPartialUpgradeLibrary
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientupgradelibrary import ClientUpgradeLibrary


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
