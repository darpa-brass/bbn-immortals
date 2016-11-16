#!/usr/bin/env python

import logging
import os

import deploymentplatform
import docker
from configurationmanager import AndroidApplicationConfig

_CONTAINER_NAME = os.environ['USER'] + '-ds'


class AndroidStaticAnalysisInstance(deploymentplatform.DeploymentPlatform):
    """
    :type config: AndroidApplicationConfig
    """

    def __init__(self, application_configuration):
        self.config = application_configuration
        self.docker = docker.DockerInstance(self.config.application_deployment_directory, application_configuration)

    def platform_setup(self):
        self.docker.platform_setup()

    def deploy_application(self, application_location):
        self.apk_filename = os.path.basename(application_location)
        target_filepath = os.path.join('/bbnAnalysis/apks/', self.apk_filename)
        self.docker.copy_file_to_docker(application_location, target_filepath)

    def upload_file(self, source_file_location, file_target):
        self.docker.upload_file(source_file_location, file_target)

    def start_application(self):
        file_found = False;
        for filepath in self.docker.files:
            if os.path.basename(filepath) == 'android_staticanalysis.sh':
                file_found = True
                cmd = ['bash', filepath, self.apk_filename]

                logging.info(
                        "Performing static analysis on '" + self.apk_filename + "'.  Please be patient as this will take several minutes...")
                self.docker.call(cmd)

                logging.info("Static analysis complete.")

                basename = os.path.splitext(os.path.basename(self.apk_filename))[0]
                self.docker.copy_file_from_docker('/bbnAnalysis/output/' + basename + 'dir.log',
                                                  self.config.application_deployment_directory)
                self.docker.copy_file_from_docker('/bbnAnalysis/output/' + basename + 'dir.output',
                                                  self.config.application_deployment_directory)
                self.docker.copy_file_from_docker('/bbnAnalysis/output/' + basename + 'dir.ddg.global.dot',
                                                  self.config.application_deployment_directory)

        if not file_found:
            raise Exception("No file has been found in the droidscope instance for android_staticanalysis.sh!!!")

    def stop_application(self):
        self.docker.stop_application()

    def platform_teardown(self):
        self.docker.platform_teardown()
