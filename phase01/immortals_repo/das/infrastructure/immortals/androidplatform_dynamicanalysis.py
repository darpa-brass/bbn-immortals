#!/usr/bin/env python

import argparse
import datetime
import logging
import os
import shutil
import subprocess
import sys
import time
import uuid

import adbhelper

import docker
import androidplatform_emulator
import deploymentplatform
from utils import replace

from configurationmanager import ADB_BIN, DAS_ROOT, EMULATOR_BIN

parser = argparse.ArgumentParser(description='IMMoRTALS Droidscope Utility')
argumentCommandGroup = parser.add_mutually_exclusive_group()
argumentCommandGroup.add_argument('-sd', '--start-docker', action='store_true', help='Start the docker container')
argumentCommandGroup.add_argument('-se', '--start-emulator', action='store_true', help='Start the docker container')

class AndroidDynamicAnalysisInstance(deploymentplatform.DeploymentPlatform):

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
                        print 'Loading droidscope tracing plugin...'
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


    def __init__(self, execution_path, application_configuration):
        self.docker = docker.DockerInstance(execution_path, application_configuration)
        self.emulator = androidplatform_emulator.AndroidEmulatorInstance(execution_path, application_configuration)
        self.emulator.call = self.docker.call
        self.emulator.Popen = self.docker.Popen
        self.emulator.check_output = self.docker.check_output
        self.emulator._start_emulator = self._start_emulator
        self.emulator._kill_emulator = self._kill_emulator
        self.emulator._delete_emulator = self._delete_emulator
        self.emulator._create_emulator = self._create_emulator
        self.emulator._emulator_exists = self._emulator_exists

        self.execution_path = execution_path
        self.config = application_configuration
        self.identifier = application_configuration.instance_identifier


    def platform_setup(self):
        self.docker.platform_setup()

        sdcard_filepath = os.path.join(self.execution_path, self.identifier + '_sdcard.img')
        cmd = ['cp', '/droidscope_sdcard.img', sdcard_filepath]
        logging.debug('EXEC: ' + str(cmd))
        self.docker.call(cmd)

        self.emulator.platform_setup()


    def deploy_application(self, application_location):
        self.docker.deploy_application(application_location)
        self.emulator.deploy_application(application_location)

        for filepath in self.docker.files:
            if os.path.basename(filepath) == 'ds_hookup_package.expect':
                print 'Hooking package "' + self.docker.config.package_identifier + '" up to droidscope...'
                cmd = ['expect', '-f', filepath, self.docker.config.package_identifier]
                self.docker.call(cmd)


    def upload_file(self, source_file_location, file_target):
        self.docker.upload_file(source_file_location, file_target)
        self.emulator.upload_file(source_file_location, file_target)


    def start_application(self, event_listener):
        self.docker.start_application(event_listener)
        self.emulator.start_application(event_listener)


    def stop_application(self):
        self.emulator.stop_application()
        self.docker.stop_application()

        self.docker.copy_file_from_docker('/decaf.log', self.execution_path)
        self.docker.copy_file_from_docker('/' + self.docker.config.package_identifier + '_jumps.log', self.execution_path)


    def platform_teardown(self):
        self.docker.platform_teardown()
