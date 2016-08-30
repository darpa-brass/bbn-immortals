"""
Represents an android application and the common configuration options necessary
"""

import logging
import os
import atexit
import sys
import threading
import time

from immortalsglobals import ImmortalsGlobals
from packages import commentjson
from packages.commentjson import JSONLibraryException

import platformhelper

def _exit_handler():
    logging.info('Cleaning up after androidapplication.py')
    for application in _instances.values():
        if application.is_application_running:
            application.stop_application()


atexit.register(_exit_handler)

_instances = {}

class AndroidApplication:

    def __init__(self, execution_path, application_configuration):
        if application_configuration.instance_identifier in _instances:
            raise Exception('An AndroidApplication with the identifier "' + application_configuration.instance_identifier + '" has already been defined!')
        else:
            _instances[application_configuration.instance_identifier] = self

        self.application_identifier = application_configuration.application_identifier
        self.instance_identifier = application_configuration.instance_identifier
        self.is_application_running = False
        self.platform = platformhelper.create_platform_instance(execution_path, application_configuration)
        self.config = application_configuration
        self.root_path = execution_path


    def setup_start_environment(self):
        self.platform.platform_setup()


    """
    deploys the configuration files and apk to the device.
    """
    def setup_application(self):
        if self.is_application_running is True:
        # if self.is_application_running is True:
            raise Exception('The environment named "' + self.identifier + '" is already running an application!')

        self.platform.deploy_application(self.config.executable_filepath)

        for source_filepath in self.config.files.keys():
            target_filepath = self.config.files[source_filepath]
            self.platform.upload_file(source_filepath, target_filepath)
        # This is only necessary on API 23+
        # if configurationmanager.Configuration.deployment_environments[self.config.deployment_platform_environment].sdk_level >= 23:
            # for permission in self.config.permissions:
                # self.platform.grant_permission(self.config.package_identifier, permission)


    """
    Starts the application and calls the event_listener when formatted events are received from the device via logcat
    """
    def start_application(self, event_listener):
        self.platform.start_application(event_listener)
        self.is_application_running = True


    """
    Forcefully stops the application if it is running
    """
    def stop_application(self):
        self.platform.stop_application()
        self.is_application_running = False


    def stop_environment(self):
        self.platform.platform_teardown()
