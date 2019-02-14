import subprocess
from abc import abstractmethod

# from . import threadprocessrouter as tpr
from pymmortals.datatypes.scenariorunnerconfiguration import ApplicationConfig
from pymmortals.scenariorunner.deploymentplatform import LifecycleInterface, DeploymentPlatformInterface
from pymmortals.scenariorunner.platforms import platformhelper
from pymmortals.threadprocessrouter import ImmortalsSubprocess

PIPE = subprocess.PIPE


class AbstractApplication(ImmortalsSubprocess, LifecycleInterface):
    def __init__(self, application_configuration: ApplicationConfig):
        self.___config = application_configuration
        self.___platform = platformhelper.create_platform_instance(application_configuration=application_configuration)

        super().__init__(command_processor=self.___platform, log_tag=application_configuration.instanceIdentifier)

    @property
    def platform(self) -> DeploymentPlatformInterface:
        return self.___platform

    @property
    def config(self) -> ApplicationConfig:
        return self.___config

    @abstractmethod
    def _stop(self):
        raise NotImplementedError

    @abstractmethod
    def clean(self):
        raise NotImplementedError

    @abstractmethod
    def _destroy(self):
        raise NotImplementedError

    @abstractmethod
    def _is_ready(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def _is_setup(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def _start(self):
        raise NotImplementedError

    @abstractmethod
    def setup(self):
        raise NotImplementedError

    @abstractmethod
    def _is_running(self) -> bool:
        raise NotImplementedError
