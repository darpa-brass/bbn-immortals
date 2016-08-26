"""
An android-specific docker platform.  Parts could probably be abstracted out
into a separate docker platform for java if necessary later on.
"""
import argparse
import logging
import os
import shutil
import signal
import sys
import time

from utils import path_helper, replace

import androidplatform_emulator
import docker
import docker

import configurationmanager
from configurationmanager import DAS_ROOT
from configurationmanager import ADB_BIN
from configurationmanager import EMULATOR_BIN
from configurationmanager import MKSDCARD_BIN
import deploymentplatform

_IDSHARED = '$SHARED!'
_IDDS_SHARED = '$DS_SHARED!'
_IDSOURCE_FILE = '$SOURCE_FILE!'
_IDTARGET_FILE = '$TARGET_FILE!'
_IDCONTAINER_NAME = '$CONTAINER_NAME!'
_IDIMAGE_IDENTIFIER = '$IMAGE_IDENTIFIER!'

_CMD_DOCKER_EXEC = ('sudo', 'docker', 'exec', _IDCONTAINER_NAME)
_CMD_GET_DOCKER_CREATED_CONTAINER_IDENTIFIER = ('sudo', 'docker', 'ps', '-a', '-q', '--filter=name=' + _IDCONTAINER_NAME)
_CMD_GET_DOCKER_RUNNING_CONTAINER_IDENTIFIER = ('sudo', 'docker', 'ps', '-q', '--filter=name=' + _IDCONTAINER_NAME)
_CMD_CREATE_CONTAINER = ('sudo', 'docker', 'run', '-itd', '--device=/dev/kvm' ,'--name=' + _IDCONTAINER_NAME, _IDIMAGE_IDENTIFIER)

_CMD_START_CONTAINER = ('sudo', 'docker', 'start', _IDCONTAINER_NAME)
_CMD_STOP_CONTAINER = ('sudo', 'docker', 'stop', _IDCONTAINER_NAME)
_CMD_DELETE_CONTAINER = ('sudo', 'docker', 'rm', _IDCONTAINER_NAME)
_CMD_COPY_TO_DOCKER = ('sudo', 'docker', 'cp', _IDSOURCE_FILE, _IDCONTAINER_NAME + ':' + _IDTARGET_FILE)


class AndroidDockerEmulatorInstance(deploymentplatform.DeploymentPlatform):

    def __init__(self, execution_path, application_configuration, clobber_existing):
        self.docker = docker.DockerInstance(execution_path, application_configuration, clobber_existing)
        self.emulator = androidplatform_emulator.AndroidEmulatorInstance(execution_path, application_configuration, clobber_existing)
        self.emulator.call = self.docker.call
        self.emulator.Popen = self.docker.Popen
        self.emulator.check_output = self.docker.check_output


    def platform_setup(self):
        self.docker.platform_setup()
        self.emulator.platform_setup()


    def deploy_application(self, application_location):
        self.docker.deploy_application(application_location)
        self.emulator.deploy_application(application_location)


    def upload_file(self, source_file_location, file_target):
        self.docker.upload_file(source_file_location, file_target)
        self.emulator.upload_file(source_file_location, file_target)


    def start_application(self, event_listener):
        self.docker.start_application(event_listener)
        self.emulator.start_application(event_listener)


    def stop_application(self):
        self.emulator.stop_application()
        self.docker.stop_application()


    def platform_teardown(self):
        self.emulator.platform_teardown()
        self.emulator.platform_teardown()
