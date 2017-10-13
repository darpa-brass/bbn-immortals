from abc import abstractmethod

from pymmortals.datatypes.scenariorunnerconfiguration import ApplicationConfig
from pymmortals.threadprocessrouter import ImmortalsSubprocess

_adb_has_been_reinitialized = False
_adb_identifier_count = 0
_emulator_name_template = 'emulator-{CONSOLEPORT}'
_port_base = 5560


def generate_emulator_identifier():
    global _adb_identifier_count

    number = _adb_identifier_count
    _adb_identifier_count += 1

    int_identifier = None
    if type(number) is int:
        int_identifier = number
    elif type(number) is str:
        int_identifier = int(number)

    if int_identifier >= 22:
        raise Exception("No more than 22 devices are supported at this time!")

    consoleport = _port_base + 2 * int_identifier
    return _emulator_name_template.format(CONSOLEPORT=consoleport)


class LifecycleInterface:
    @abstractmethod
    def setup(self):
        """
        Set up the object so it is ready to be started
        """
        raise NotImplementedError

    @abstractmethod
    def _stop(self):
        """
        Stop the running object
        """
        raise NotImplementedError

    @abstractmethod
    def _destroy(self):
        """
        Destroy the object
        """
        raise NotImplementedError

    @abstractmethod
    def _is_running(self) -> bool:
        """
        Is the object running?
        """
        raise NotImplementedError

    @abstractmethod
    def _is_setup(self) -> bool:
        """
        Is the object set up?
        """
        raise NotImplementedError

    @abstractmethod
    def clean(self):
        """
        Attempt to clean the object for reuse.
        """
        raise NotImplementedError

    @abstractmethod
    def _start(self):
        """
        Start the object
        """
        raise NotImplementedError

    def stop(self):
        if self.is_running():
            self._stop()

    def destroy(self):
        self.stop()
        if self.is_setup():
            self._destroy()

    def start(self):
        if not self.is_running():
            self._start()

    @abstractmethod
    def _is_ready(self) -> bool:
        """
        Is the object in a state to be used?
        """
        raise NotImplementedError

    def is_ready(self) -> bool:
        """
        Is the object in a state to be used? (This will account for it not running or being setup
        """
        return self.is_running() and self._is_ready()

    def is_running(self) -> bool:
        """
        Is the object running? (This will account for it not being setup
        """
        return self.is_setup() and self._is_running()

    def is_setup(self) -> bool:
        """
        Is the object set up?
        """
        return self._is_setup()


class DeploymentPlatformInterface(ImmortalsSubprocess, LifecycleInterface):
    """
    Core application deployment platform interface
    """

    def __init__(self, application_configuration: ApplicationConfig,
                 command_processor: ImmortalsSubprocess):
        self.___config = application_configuration
        super().__init__(command_processor=command_processor, log_tag=application_configuration.instanceIdentifier)

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

    @abstractmethod
    def deploy_application(self, application_location: str):
        raise NotImplementedError

    @abstractmethod
    def application_stop(self):
        raise NotImplementedError

    @abstractmethod
    def application_start(self):
        raise NotImplementedError

    @abstractmethod
    def upload_file(self, source_file_location: str, file_target: str):
        raise NotImplementedError

    @abstractmethod
    def application_destroy(self):
        raise NotImplementedError
