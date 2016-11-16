"""
An android-specific docker platform.  Parts could probably be abstracted out
into a separate docker platform for java if necessary later on.
"""
import atexit
import logging
import os
import shutil
import subprocess
import uuid
from threading import Lock

import configurationmanager
import deploymentplatform
from configurationmanager import DAS_ROOT
from utils import path_helper, replace

_halt_identifiers = []
_remove_identifiers = []


def _exit_handler():
    for identifier in _halt_identifiers:
        try:
            subprocess.call(['sudo', 'docker', 'stop', identifier], stdout=None, stderr=None)
        except:
            pass

            # for identifier in _remove_identifiers:
            # try:
            # subprocess.call(['sudo', 'docker', 'rm', identifier], stdout=None, stderr=None)
            # except:
            # pass


atexit.register(_exit_handler)

_IDSHARED = '$SHARED!'
_IDDS_SHARED = '$DS_SHARED!'
_IDSOURCE_FILE = '$SOURCE_FILE!'
_IDTARGET_FILE = '$TARGET_FILE!'
_IDCONTAINER_NAME = '$CONTAINER_NAME!'
_IDIMAGE_IDENTIFIER = '$IMAGE_IDENTIFIER!'

_CMD_DOCKER_EXEC = ('sudo', 'docker', 'exec', _IDCONTAINER_NAME)
_CMD_GET_DOCKER_CREATED_CONTAINER_IDENTIFIER = (
    'sudo', 'docker', 'ps', '-a', '-q', '--filter=name=' + _IDCONTAINER_NAME)
_CMD_GET_DOCKER_RUNNING_CONTAINER_IDENTIFIER = ('sudo', 'docker', 'ps', '-q', '--filter=name=' + _IDCONTAINER_NAME)
_CMD_CREATE_CONTAINER = (
    'sudo', 'docker', 'run', '-itd', '--device=/dev/kvm', '--name=' + _IDCONTAINER_NAME, _IDIMAGE_IDENTIFIER)

_CMD_START_CONTAINER = ('sudo', 'docker', 'start', _IDCONTAINER_NAME)
_CMD_STOP_CONTAINER = ('sudo', 'docker', 'stop', _IDCONTAINER_NAME)
_CMD_DELETE_CONTAINER = ('sudo', 'docker', 'rm', _IDCONTAINER_NAME)
_CMD_COPY_TO_DOCKER = ('sudo', 'docker', 'cp', _IDSOURCE_FILE, _IDCONTAINER_NAME + ':' + _IDTARGET_FILE)


class DockerInstance(deploymentplatform.DeploymentPlatform):
    def __init__(self, execution_path, application_configuration, clobber_existing):
        self.lock = Lock()
        self.execution_path = execution_path
        self.config = application_configuration
        self.identifier = application_configuration.instance_identifier
        self.image_identifier = application_configuration.deployment_platform_environment
        self.clobber_existing = clobber_existing

    def platform_setup(self):
        running_id = self._get_docker_id_if_running()
        existing_id = self._get_docker_id_if_exists()

        if running_id:
            if self.clobber_existing:
                _halt_identifiers.append(self.identifier)
                self._stop_docker_container()
        else:
            _halt_identifiers.append(self.identifier)

        if existing_id:
            if self.clobber_existing:
                _remove_identifiers.append(self.identifier)
                self._remove_docker_container()
        else:
            _remove_identifiers.append(self.identifier)

        self.files = []

        if not existing_id or self.clobber_existing:
            self._docker_run()

        # If it is not running, start it
        cmd = list(_CMD_START_CONTAINER)
        replace(cmd, _IDCONTAINER_NAME, self.identifier)
        logging.debug('EXEC: ' + str(cmd))
        logging.info(subprocess.check_output(cmd))

        cmd = ['mkdir', '-p', self.execution_path]
        self.check_output(cmd)

        for script in configurationmanager.Configuration.scenario_runner.docker.scripts:
            filepath = path_helper(True, DAS_ROOT, script)
            self.files.append(filepath)
            self.copy_file_to_docker(filepath, filepath)

    def deploy_application(self, apk_location):
        self.copy_file_to_docker(apk_location, apk_location)

    def upload_file(self, source_file_location, file_target):
        self.copy_file_to_docker(source_file_location, source_file_location)

    def start_application(self, event_listener):
        pass

    def stop_application(self):
        pass

    def platform_teardown(self):
        if self.identifier in _halt_identifiers:
            self._stop_docker_container()

            # Not doing since it makes analysis of issues impossible
            # if self.identifier in _remove_identifiers:
            # self._remove_docker_container()

    def _get_docker_id_if_running(self):
        # Command to query for the container name in all containers
        cmd = list(_CMD_GET_DOCKER_RUNNING_CONTAINER_IDENTIFIER)
        replace(cmd, _IDCONTAINER_NAME, self.identifier)

        # If it does not exist, create it
        logging.debug('EXEC: ' + str(cmd))
        val = subprocess.check_output(cmd)

        if val == "":
            return None
        else:
            return val

    def _docker_run(self, additional_parameters=None):
        cmd = list(_CMD_CREATE_CONTAINER)
        replace(cmd, _IDCONTAINER_NAME, self.identifier)

        if additional_parameters is not None:
            for parameter in additional_parameters:
                cmd.insert(len(cmd) - 1, parameter)

        replace(cmd, _IDIMAGE_IDENTIFIER, self.image_identifier)
        logging.debug('EXEC: ' + str(cmd))
        logging.info(subprocess.check_output(cmd))

    def _get_docker_id_if_exists(self):
        # Command to query for the container name in all containers
        cmd = list(_CMD_GET_DOCKER_CREATED_CONTAINER_IDENTIFIER)
        replace(cmd, _IDCONTAINER_NAME, self.identifier)

        # If it does not exist, create it
        logging.debug('EXEC: ' + str(cmd))
        val = subprocess.check_output(cmd)

        if val == "":
            return None
        else:
            return val

    def _stop_docker_container(self):
        with self.lock:
            if self._get_docker_id_if_running():
                cmd = list(_CMD_STOP_CONTAINER)
                replace(cmd, _IDCONTAINER_NAME, self.identifier)
                logging.debug('EXEC: ' + str(cmd))
                subprocess.call(cmd)

    def _remove_docker_container(self):
        with self.lock:
            if self._get_docker_id_if_exists():
                cmd = list(_CMD_DELETE_CONTAINER)
                replace(cmd, _IDCONTAINER_NAME, self.identifier)
                logging.debug('EXEC: ' + str(cmd))
                subprocess.call(cmd)

    def call(self, call_list):
        cmd = list(_CMD_DOCKER_EXEC) + call_list
        replace(cmd, _IDCONTAINER_NAME, self.identifier)

        logging.debug('EXEC' + str(cmd))
        subprocess.call(cmd)

    def check_output(self, call_list):
        cmd = list(_CMD_DOCKER_EXEC) + call_list
        replace(cmd, _IDCONTAINER_NAME, self.identifier)

        logging.debug('EXEC' + str(cmd))
        return subprocess.check_output(cmd)

    def Popen(self, args, bufsize=0, executable=None, stdin=None, stdout=None, stderr=None, preexec_fn=None,
              close_fds=False, shell=False, cwd=None, env=None, universal_newlines=False, startupinfo=None,
              creationflags=0):
        cmd = list(_CMD_DOCKER_EXEC) + args
        replace(cmd, _IDCONTAINER_NAME, self.identifier)

        logging.debug('EXEC' + str(cmd))
        return subprocess.Popen(cmd, bufsize, executable, stdin, stdout, stderr, preexec_fn, close_fds, shell, cwd, env,
                                universal_newlines, startupinfo, creationflags)

    def copy_file_to_docker(self, local_filepath, target_filepath):

        cmd = ['mkdir', '-p', os.path.dirname(target_filepath)]
        self.check_output(cmd)

        cmd = list(_CMD_COPY_TO_DOCKER)
        cmd = replace(cmd, _IDCONTAINER_NAME, self.identifier)
        cmd = replace(cmd, _IDSOURCE_FILE, local_filepath)
        cmd = replace(cmd, _IDTARGET_FILE, target_filepath)
        logging.debug('EXEC: ' + str(cmd))
        subprocess.call(cmd)

    def copy_file_from_docker(self, docker_filepath, target_directory):
        filename = os.path.basename(docker_filepath)
        tmptarget = os.path.join('/tmp/', str(uuid.uuid4()))
        finaltarget = os.path.join(target_directory, filename)

        cmd = ['sudo', 'docker', 'cp', self.identifier + ':' + docker_filepath, tmptarget]
        logging.debug('EXEC: ' + str(cmd))
        subprocess.call(cmd)

        shutil.copyfile(tmptarget, finaltarget)

        cmd = ['sudo', 'rm', tmptarget]
        logging.debug('EXEC: ' + str(cmd))
        subprocess.call(cmd)
