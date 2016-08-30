
import atexit
import logging
import os
import signal

from javaplatform import JavaPlatform
from utils import path_helper

def _exit_handler():
    for application in _instances.values():
        if application.is_application_running:
            application.stop_application()

atexit.register(_exit_handler)

_instances = {}


class JavaApplication:

    def __init__(self, execution_path, application_configuration):
        if application_configuration.instance_identifier in _instances:
            raise Exception('A JavaApplication with the identifier "' + application_configuration.instance_identifier + '" has already been defined!')
        else:
            _instances[application_configuration.instance_identifier] = self

        self.instance_identifier = application_configuration.instance_identifier
        self.application_identifier = application_configuration.application_identifier
        self.is_application_running = False
        self.environment = JavaPlatform(execution_path, application_configuration)
        self.config = application_configuration
        self.root_path = execution_path
        self.jar_filepath = path_helper(False, self.root_path, os.path.basename(self.config.executable_filepath))
        self.files = {}

        for src_filepath in self.config.files.keys():
            self.files[src_filepath] = path_helper(False, self.root_path, self.config.files[src_filepath])


    def setup_start_environment(self):
        self.environment.platform_setup()


    def stop_environment(self):
        self.environment.platform_teardown()


    """
    Forcefully stops the application if it is running
    """
    def stop_application(self):
        self.environment.force_stop_process(self.instance_identifier)
        self.is_application_running = False

    """
    deploys the configuration files and apk to the device.

    # , modifying the
    # configuration files if necessary (such as with the identifier)
    """
    def setup_application(self):
        if self.is_application_running is True:
            raise Exception('The environment named "' + self.identifier + '" is already running an application!')

        self.environment.copy_file(self.config.executable_filepath, self.jar_filepath)

        for src_filepath in self.files.keys():
            self.environment.copy_file(src_filepath, self.files[src_filepath])


    """
    Starts the application and calls the event_listener when formatted events are received from the device via logcat
    """
    def start_application(self, event_listener):
        self.is_application_running = True

        with open(os.path.join(self.root_path, 'stdout_log.txt'), 'w') as stdout_log, open(os.path.join(self.root_path, 'stderr_log.txt'), 'w') as stderr_log:
            self.application_process = self.environment.start_jar(self.jar_filepath, stdout_log, stderr_log)
