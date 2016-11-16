#!/usr/bin/env python

import argparse
import logging
import os

import androidplatform_emulator
import deploymentplatform
import docker
from configurationmanager import AndroidApplicationConfig

parser = argparse.ArgumentParser(description='IMMoRTALS Droidscope Utility')
argumentCommandGroup = parser.add_mutually_exclusive_group()
argumentCommandGroup.add_argument('-sd', '--start-docker', action='store_true', help='Start the docker container')
argumentCommandGroup.add_argument('-se', '--start-emulator', action='store_true', help='Start the docker container')


class AndroidDynamicAnalysisInstance(deploymentplatform.DeploymentPlatform):
    """
    :type config: AndroidApplicationConfig
    """

    def _start_emulator(self, console_port, display_ui, sdcard_filepath):
        for filepath in self.docker.files:
            if os.path.basename(filepath) == 'ds_start_emulator.sh':
                cmd = ['bash', filepath, console_port, sdcard_filepath]
                logging.debug('EXEC: ' + str(cmd))
                self.docker.Popen(cmd)
                self.emulator.adbhelper.wait_for_device_ready()

                cmd = ['adb', 'shell', 'setprop', 'dalvik.vm.dex2oat-filter', '"speed"']
                logging.debug('EXEC: ' + str(cmd))
                self.docker.call(cmd)

                for filepath in self.docker.files:
                    if os.path.basename(filepath) == 'ds_load_tracing_plugin.expect':
                        logging.info('Loading droidscope tracing plugin...')
                        cmd = ['expect', filepath]
                        logging.debug('EXEC: ' + str(cmd))
                        self.docker.call(cmd)

    def _kill_emulator(self):
        pass

    def _delete_emulator(self):
        pass

    def _create_emulator(self):
        pass

    def _emulator_exists(self):
        return False

    def __init__(self, application_configuration):
        self.config = application_configuration
        self.docker = docker.DockerInstance(self.config.application_deployment_directory, application_configuration)
        self.emulator = androidplatform_emulator.AndroidEmulatorInstance(self.config.application_deployment_directory,
                                                                         application_configuration)
        self.emulator.call = self.docker.call
        self.emulator.Popen = self.docker.Popen
        self.emulator.check_output = self.docker.check_output
        self.emulator._start_emulator = self._start_emulator
        self.emulator._kill_emulator = self._kill_emulator
        self.emulator._delete_emulator = self._delete_emulator
        self.emulator._create_emulator = self._create_emulator
        self.emulator._emulator_exists = self._emulator_exists

        self.config = application_configuration

    def platform_setup(self):
        self.docker.platform_setup()

        sdcard_filepath = os.path.join(self.config.application_deployment_directory,
                                       self.config.application_identifier + '_sdcard.img')
        cmd = ['cp', '/droidscope_sdcard.img', sdcard_filepath]
        logging.debug('EXEC: ' + str(cmd))
        self.docker.call(cmd)

        self.emulator.platform_setup()

    def deploy_application(self, application_location):
        self.docker.deploy_application(application_location)
        self.emulator.deploy_application(application_location)

        for filepath in self.docker.files:
            if os.path.basename(filepath) == 'ds_hookup_package.expect':
                logging.info('Hooking package "' + self.docker.config.package_identifier + '" up to droidscope...')
                cmd = ['expect', '-f', filepath, self.docker.config.package_identifier]
                self.docker.call(cmd)

    def upload_file(self, source_file_location, file_target):
        self.docker.upload_file(source_file_location, file_target)
        self.emulator.upload_file(source_file_location, file_target)

    def start_application(self):
        self.docker.start_application()
        self.emulator.start_application()

    def stop_application(self):
        self.emulator.stop_application()
        self.docker.stop_application()

        self.docker.copy_file_from_docker('/decaf.log', self.config.application_deployment_directory)
        self.docker.copy_file_from_docker('/' + self.docker.config.package_identifier + '_jumps.log',
                                          self.config.application_deployment_directory)

    def platform_teardown(self):
        self.docker.platform_teardown()
