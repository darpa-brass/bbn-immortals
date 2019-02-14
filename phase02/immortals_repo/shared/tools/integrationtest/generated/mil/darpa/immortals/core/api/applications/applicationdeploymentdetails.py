from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.applications.applicationtype import ApplicationType
from integrationtest.generated.mil.darpa.immortals.core.api.applications.deploymentplatform import DeploymentPlatform


# noinspection PyPep8Naming
class ApplicationDeploymentDetails(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 applicationBasePath: str = None,
                 applicationTargetPath: str = None,
                 compositionTarget: ApplicationType = None,
                 deploymentPlatform: DeploymentPlatform = None,
                 gradleModificationFile: str = None,
                 sessionIdentifier: str = None):
        super().__init__()
        self.applicationBasePath = applicationBasePath
        self.applicationTargetPath = applicationTargetPath
        self.compositionTarget = compositionTarget
        self.deploymentPlatform = deploymentPlatform
        self.gradleModificationFile = gradleModificationFile
        self.sessionIdentifier = sessionIdentifier
