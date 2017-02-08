"""
An android-specific docker platform.  Parts could probably be abstracted out
into a separate docker platform for java if necessary later on.
"""
import logging
import os
import shutil
import uuid
from threading import Lock

import immortalsglobals as ig
from data.applicationconfig import ApplicationConfig
from deploymentplatform import DeploymentPlatformInterface
from immortalsglobals import IMMORTALS_ROOT
from interfaces import CommandHandlerInterface
from utils import replace
from .data.base.tools import path_helper

_halt_identifiers = []
_remove_identifiers = []

_ID_SHARED = '$SHARED!'
_ID_DS_SHARED = '$DS_SHARED!'
_ID_SOURCE_FILE = '$SOURCE_FILE!'
_ID_TARGET_FILE = '$TARGET_FILE!'
ID_CONTAINER_NAME = '$CONTAINER_NAME!'
_ID_IMAGE_IDENTIFIER = '$IMAGE_IDENTIFIER!'

_CMD_DOCKER_EXEC = ('sudo', 'docker', 'exec', ID_CONTAINER_NAME)
_CMD_GET_DOCKER_CREATED_CONTAINER_IDENTIFIER = (
    'sudo', 'docker', 'ps', '-a', '-q', '--filter=name=' + ID_CONTAINER_NAME)
_CMD_GET_DOCKER_RUNNING_CONTAINER_IDENTIFIER = ('sudo', 'docker', 'ps', '-q', '--filter=name=' + ID_CONTAINER_NAME)
_CMD_CREATE_CONTAINER = (
    'sudo', 'docker', 'run', '-itd', '--device=/dev/kvm', '--name=' + ID_CONTAINER_NAME, _ID_IMAGE_IDENTIFIER)

_CMD_START_CONTAINER = ('sudo', 'docker', 'start', ID_CONTAINER_NAME)
_CMD_STOP_CONTAINER = ('sudo', 'docker', 'stop', ID_CONTAINER_NAME)
_CMD_DELETE_CONTAINER = ('sudo', 'docker', 'rm', ID_CONTAINER_NAME)
_CMD_COPY_TO_DOCKER = ('sudo', 'docker', 'cp', _ID_SOURCE_FILE, ID_CONTAINER_NAME + ':' + _ID_TARGET_FILE)


class DockerInstance(DeploymentPlatformInterface):
    """
    :type config: ApplicationConfig
    """

    def application_stop(self):
        raise NotImplementedError

    def application_start(self):
        raise NotImplementedError

    def __init__(self, application_configuration, command_processor=None):
        DeploymentPlatformInterface.__init__(self, command_processor=command_processor)

        self.config = application_configuration
        self.lock = Lock()

    def setup(self):
        running_id = self._get_docker_id_if_running()
        existing_id = self._get_docker_id_if_exists()

        if running_id:
            if ig.configuration.validationEnvironment.setupEnvironmentLifecycle.destroyExisting:
                _halt_identifiers.append(self.config.instanceIdentifier)
                self._stop_docker_container()
        else:
            _halt_identifiers.append(self.config.instanceIdentifier)

        if existing_id:
            if ig.configuration.validationEnvironment.setupEnvironmentLifecycle.destroyExisting:
                _remove_identifiers.append(self.config.instanceIdentifier)
                self._remove_docker_container()
        else:
            _remove_identifiers.append(self.config.instanceIdentifier)

        files = []

        if not existing_id or ig.configuration.validationEnvironment.setupEnvironmentLifecycle.destroyExisting:
            self._docker_run()

        # If it is not running, start it
        cmd = list(_CMD_START_CONTAINER)
        replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)
        logging.info(self.check_output(cmd))

        cmd = ['mkdir', '-p', self.config.applicationDeploymentDirectory]
        self.check_output(cmd)

        for script in ig.configuration.scenarioRunner.docker.scripts:
            filepath = path_helper(True, IMMORTALS_ROOT, script)
            files.append(filepath)
            self.copy_file_to_docker(filepath, filepath)

    def deploy_application(self, apk_location):
        self.copy_file_to_docker(apk_location, apk_location)

    def upload_file(self, source_file_location, file_target):
        self.copy_file_to_docker(source_file_location, source_file_location)

    def stop(self):
        if self.config.instanceIdentifier in _halt_identifiers:
            self._stop_docker_container()

            # Not doing since it makes analysis of issues impossible
            # if self.config.instance_identifier in _remove_identifiers:
            # self._remove_docker_container()

    def _get_docker_id_if_running(self):
        # Command to query for the container name in all containers
        cmd = list(_CMD_GET_DOCKER_RUNNING_CONTAINER_IDENTIFIER)
        replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)

        # If it does not exist, create it
        val = self.check_output(cmd)

        if val == "":
            return None
        else:
            return val

    def _docker_run(self, additional_parameters=None):
        cmd = list(_CMD_CREATE_CONTAINER)
        replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)

        if additional_parameters is not None:
            for parameter in additional_parameters:
                cmd.insert(len(cmd) - 1, parameter)

        replace(cmd, _ID_IMAGE_IDENTIFIER, self.config.deploymentPlatformEnvironment)
        logging.info(self.check_output(cmd))

    def _get_docker_id_if_exists(self):
        # Command to query for the container name in all containers
        cmd = list(_CMD_GET_DOCKER_CREATED_CONTAINER_IDENTIFIER)
        replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)

        # If it does not exist, create it
        val = self.check_output(cmd)

        if val == "":
            return None
        else:
            return val

    def _stop_docker_container(self):
        with self.lock:
            if self._get_docker_id_if_running():
                cmd = list(_CMD_STOP_CONTAINER)
                replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)
                self.call(cmd)

    def _remove_docker_container(self):
        with self.lock:
            if self._get_docker_id_if_exists():
                cmd = list(_CMD_DELETE_CONTAINER)
                replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)
                self.call(cmd)

    def call(self, args, halt_on_shutdown=None, shutdown_method=None, shutdown_args=(), stdout=None, stderr=None,
             *popenargs, **kwargs):

        cmd = list(_CMD_DOCKER_EXEC) + args
        args = replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)

        return CommandHandlerInterface.call(self, args, halt_on_shutdown, shutdown_method, shutdown_args, stdout,
                                            stderr, *popenargs, **kwargs)

    def check_call(self, args, halt_on_shutdown=None, shutdown_method=None, shutdown_args=(), stdout=None, stderr=None,
                   *popenargs, **kwargs):

        cmd = list(_CMD_DOCKER_EXEC) + args
        args = replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)
        return CommandHandlerInterface.check_call(self, args, halt_on_shutdown, shutdown_method, shutdown_args, stdout,
                                                  stderr, *popenargs, **kwargs)

    def check_output(self, args, halt_on_shutdown=None, shutdown_method=None, shutdown_args=(), stderr=None, *popenargs,
                     **kwargs):

        cmd = list(_CMD_DOCKER_EXEC) + args
        args = replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)
        return CommandHandlerInterface.check_output(self, args, halt_on_shutdown, shutdown_method, shutdown_args,
                                                    stderr, *popenargs, **kwargs)

    def Popen(self, args, halt_on_shutdown=None, shutdown_method=None, shutdown_args=(), stdout=None, stderr=None,
              *popenargs, **kwargs):

        cmd = list(_CMD_DOCKER_EXEC) + args
        args = replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)
        return CommandHandlerInterface.Popen(self, args, halt_on_shutdown, shutdown_method, shutdown_args, stdout,
                                             stderr, *popenargs, **kwargs)

    def copy_file_to_docker(self, local_filepath, target_filepath):

        cmd = ['mkdir', '-p', os.path.dirname(target_filepath)]
        self.check_output(cmd)

        cmd = list(_CMD_COPY_TO_DOCKER)
        cmd = replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)
        cmd = replace(cmd, _ID_SOURCE_FILE, local_filepath)
        cmd = replace(cmd, _ID_TARGET_FILE, target_filepath)
        self.call(cmd)

    def copy_file_from_docker(self, docker_filepath, target_directory):
        filename = os.path.basename(docker_filepath)
        tmp_target = os.path.join('/tmp/', str(uuid.uuid4()))
        final_target = os.path.join(target_directory, filename)

        cmd = ['sudo', 'docker', 'cp', self.config.instanceIdentifier + ':' + docker_filepath, tmp_target]
        self.call(cmd)

        shutil.copyfile(tmp_target, final_target)

        cmd = ['sudo', 'rm', tmp_target]
        self.call(cmd)
