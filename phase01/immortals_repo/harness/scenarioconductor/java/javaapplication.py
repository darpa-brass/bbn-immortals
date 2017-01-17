import os

from javaplatform import JavaPlatform
from ..data.applicationconfig import JavaApplicationConfig
from ..deploymentplatform import LifecycleInterface
from ..data.base.tools import path_helper


class JavaApplication(LifecycleInterface):
    """
    :type application_configuration: JavaApplicationConfig
    :type environment:  JavaPlatform
    """

    def __init__(self, application_configuration):

        self.config = application_configuration
        self.is_application_running = False
        self.is_application_setup = False
        self.platform = JavaPlatform(application_configuration)

        self.jar_filepath = path_helper(False, self.config.applicationDeploymentDirectory,
                                        os.path.basename(self.config.executableFile))

        self.files = {}

        for src_filepath in self.config.files.keys():
            self.files[src_filepath] = path_helper(False, self.config.applicationDeploymentDirectory,
                                                   self.config.files[src_filepath])

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

        for src_filepath in self.files.keys():
            self.platform.upload_file(src_filepath, self.files[src_filepath])

        self.is_application_setup = True

    def _start(self):
        self.platform.application_start()
        self.is_application_running = True
