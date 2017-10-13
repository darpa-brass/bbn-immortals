import os

from pymmortals.datatypes.scenariorunnerconfiguration import JavaApplicationConfig
from pymmortals.interfaces import AbstractApplication
from pymmortals.utils import path_helper


class JavaApplication(AbstractApplication):
    def __init__(self, application_configuration: JavaApplicationConfig):

        super().__init__(application_configuration=application_configuration)

        self.is_application_running = False
        self.is_application_setup = False

        self.jar_filepath = path_helper(False, self.config.applicationDeploymentDirectory,
                                        os.path.basename(self.config.executableFile))

        for copy_file in self.config.filesToCopy:
            copy_file.sourceFilepath = path_helper(False, self.config.applicationDeploymentDirectory,
                                                   copy_file.sourceFilepath)

    """
    Forcefully stops the application if it is running
    """

    def _stop(self):
        self.platform.application_stop()
        self.is_application_running = False

    """
    deploys the configuration files and apk to the device, modifying the
    configuration files if necessary (such as with the identifier)
    """

    def _is_ready(self):
        return self.is_application_running

    def _is_running(self):
        return self.is_application_running

    def _is_setup(self):
        return self.is_application_setup

    def _destroy(self):
        self.platform.application_destroy()
        # TODO: Implement
        pass

    def clean(self):
        # TODO: Implement
        pass

    def setup(self):
        if self.is_application_running is True:
            raise Exception(
                'The environment named "' + self.config.instanceIdentifier + '" is already running an application!')

        self.platform.upload_file(self.config.executableFile, self.jar_filepath)

        for copy_file in self.config.filesToCopy:
            self.platform.upload_file(copy_file.sourceFilepath, copy_file.targetFilepath)

        self.is_application_setup = True

    def _start(self):
        self.platform.application_start()
        self.is_application_running = True
