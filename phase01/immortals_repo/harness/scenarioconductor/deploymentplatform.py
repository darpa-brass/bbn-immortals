from interfaces import CommandHandlerInterface

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
        int_identifier = str(number)

    if int_identifier >= 22:
        raise Exception("No more than 22 devices are supported at this time!")

    consoleport = _port_base + 2 * int_identifier
    return _emulator_name_template.format(CONSOLEPORT=consoleport)


class LifecycleInterface():
    def setup(self):
        """
        Set up the object so it is ready to be started
        """
        raise NotImplementedError

    def _stop(self):
        """
        Stop the running object
        """
        raise NotImplementedError

    def _destroy(self):
        """
        Destroy the object
        """
        raise NotImplementedError

    def _is_running(self):
        """
        Is the object running?
        """
        raise NotImplementedError

    def _is_setup(self):
        """
        Is the object set up?
        """
        raise NotImplementedError

    def clean(self):
        """
        Attempt to clean the object for reuse.
        """
        raise NotImplementedError

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

    def _is_ready(self):
        """
        Is the object in a state to be used?
        """
        raise NotImplementedError

    def is_ready(self):
        """
        Is the object in a state to be used? (This will account for it not running or being setup
        """
        return self.is_running() and self._is_ready()

    def is_running(self):
        """
        Is the object running? (This will account for it not being setup
        """
        return self.is_setup() and self._is_running()

    def is_setup(self):
        """
        Is the object set up?
        """
        return self._is_setup()


class DeploymentPlatformInterface(LifecycleInterface, CommandHandlerInterface):
    """
    Core application deployment platform interface
    """

    def deploy_application(self, application_location):
        raise NotImplementedError

    def upload_file(self, source_file_location, file_target):
        raise NotImplementedError

    def application_start(self):
        raise NotImplementedError

    def application_stop(self):
        raise NotImplementedError

    def application_destroy(self):
        raise NotImplementedError
