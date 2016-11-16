"""
Represents an android application and the common configuration options necessary
"""

import atexit
import json
import logging

import platformhelper
from configurationmanager import AndroidApplicationConfig
from utils import path_helper


def _exit_handler():
    logging.info('Cleaning up after androidapplication.py')
    for application in _instances.values():
        if application.is_application_running:
            application.stop_application()


atexit.register(_exit_handler)

_instances = {}


class AndroidApplication:
    """
    :type config: AndroidApplicationConfig
    """

    def __init__(self, application_configuration):
        if application_configuration.instance_identifier in _instances:
            raise Exception(
                    'An AndroidApplication with the identifier "' + application_configuration.instance_identifier + '" has already been defined!')
        else:
            _instances[application_configuration.instance_identifier] = self

        self.config = application_configuration
        self.is_application_running = False
        self.platform = platformhelper.create_platform_instance(application_configuration)

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

        with open(self.config.configuration_template_filepath, 'r') as f:
            atak_lite_config = json.load(f)

            for key in self.config.properties.keys():
                property_path = key.split('.')

                attr_parent = None

                while len(property_path) > 1:
                    key = property_path.pop(0)
                    attr_parent = atak_lite_config[key]

                if attr_parent is None:
                    attr_parent = atak_lite_config

                attr_parent[property_path.pop(0)] = self.config.properties[key]

            fp = path_helper(False, self.config.application_deployment_directory,
                             self.config.configuration_template_filepath.split('/').pop())

            with open(fp, 'w') as f2:
                json.dump(atak_lite_config, f2)

            self.config.files[fp] = self.config.configuration_target_filepath

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

    def start_application(self):
        self.platform.start_application()
        self.is_application_running = True

    """
    Forcefully stops the application if it is running
    """

    def stop_application(self):
        self.platform.stop_application()
        self.is_application_running = False

    def stop_environment(self):
        self.platform.platform_teardown()
