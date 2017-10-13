import os
from io import BufferedIOBase
from typing import List

from pymmortals.threadprocessrouter import ImmortalsSubprocess, global_subprocess
from pymmortals.utils import replace

_ID_CONTAINER_NAME = '$CONTAINER_NAME!'
_ID_IMAGE_IDENTIFIER = '$IMAGE_IDENTIFIER!'
_ID_SHARED = '$SHARED!'
_ID_DS_SHARED = '$DS_SHARED!'
_ID_SOURCE_FILE = '$SOURCE_FILE!'
_ID_TARGET_FILE = '$TARGET_FILE!'

_CMD_GET_DOCKER_CREATED_CONTAINER_IDENTIFIER = (
    'sudo', 'docker', 'ps', '-a', '-q', '--filter=name=' + _ID_CONTAINER_NAME)

_CMD_GET_DOCKER_RUNNING_CONTAINER_IDENTIFIER = ('sudo', 'docker', 'ps', '-q', '--filter=name=' + _ID_CONTAINER_NAME)

_CMD_CREATE_CONTAINER = (
    'sudo', 'docker', 'run', '-itd', '--device=/dev/kvm', '--name=' + _ID_CONTAINER_NAME, _ID_IMAGE_IDENTIFIER)

_CMD_START_CONTAINER = ('sudo', 'docker', 'start', _ID_CONTAINER_NAME)
_CMD_STOP_CONTAINER = ('sudo', 'docker', 'stop', _ID_CONTAINER_NAME)
_CMD_DELETE_CONTAINER = ('sudo', 'docker', 'rm', _ID_CONTAINER_NAME)

_CMD_COPY_TO_DOCKER = ('sudo', 'docker', 'cp', _ID_SOURCE_FILE, _ID_CONTAINER_NAME + ':' + _ID_TARGET_FILE)

_CMD_DOCKER_EXEC = ('sudo', 'docker', 'exec', _ID_CONTAINER_NAME)

_running_containers: List[str] = list()
_existing_containers: List[str] = list()


def container_is_running(container_identifier: str, command_processor: ImmortalsSubprocess = global_subprocess):
    cmd = list(_CMD_GET_DOCKER_RUNNING_CONTAINER_IDENTIFIER)
    replace(cmd, _ID_CONTAINER_NAME, container_identifier)
    return command_processor.run(cmd).stdout != ""


def container_start(container_identifier: str, command_processor: ImmortalsSubprocess = global_subprocess):
    cmd = list(_CMD_START_CONTAINER)
    replace(cmd, _ID_CONTAINER_NAME, container_identifier)
    command_processor.run(cmd)


def container_stop(container_identifier: str, command_processor: ImmortalsSubprocess = global_subprocess):
    if container_is_running(container_identifier=container_identifier, command_processor=command_processor):
        cmd = list(_CMD_STOP_CONTAINER)
        replace(cmd, _ID_CONTAINER_NAME, container_identifier)
        command_processor.run(cmd)


def container_delete(container_identifier: str, command_processor: ImmortalsSubprocess = global_subprocess):
    cmd = list(_CMD_DELETE_CONTAINER)
    replace(cmd, _ID_CONTAINER_NAME, container_identifier)
    command_processor.run(cmd)


def container_create(container_identifier: str, image_name: str,
                     command_processor: ImmortalsSubprocess = global_subprocess):
    cmd = list(_CMD_CREATE_CONTAINER)
    replace(cmd, _ID_CONTAINER_NAME, container_identifier)
    replace(cmd, _ID_IMAGE_IDENTIFIER, image_name)
    command_processor.run(cmd)


def container_is_created(container_identifier: str, command_processor: ImmortalsSubprocess = global_subprocess):
    cmd = list(_CMD_GET_DOCKER_CREATED_CONTAINER_IDENTIFIER)
    replace(cmd, _ID_CONTAINER_NAME, container_identifier)
    return command_processor.run(cmd).stdout != ""


def copy_file_to_docker(container_identifier: str, local_filepath: str, target_filepath: str,
                        command_processor: ImmortalsSubprocess = global_subprocess):
    cmd = ['mkdir', '-p', os.path.dirname(target_filepath)]
    command_processor.run(cmd)

    cmd = list(_CMD_COPY_TO_DOCKER)
    cmd = replace(cmd, _ID_CONTAINER_NAME, container_identifier)
    cmd = replace(cmd, _ID_SOURCE_FILE, local_filepath)
    cmd = replace(cmd, _ID_TARGET_FILE, target_filepath)
    command_processor.run(cmd)


class DockerHelper(ImmortalsSubprocess):
    def __init__(self, container_identifier: str, image_identifier: str, log_tag: str,
                 command_processor: ImmortalsSubprocess = global_subprocess):
        super().__init__(command_processor, log_tag)
        self.container_identifier: str = container_identifier
        self.image_identifier: str = image_identifier
        self._docker_host_command_processor: ImmortalsSubprocess = command_processor

    def Popen(self, args, bufsize=-1, executable=None, stdin=None, stdout: BufferedIOBase = None,
              stderr: BufferedIOBase = None, preexec_fn=None, close_fds=True, shell=False, cwd=None, env=None,
              universal_newlines=False, startupinfo=None, creationflags=0, restore_signals=True,
              start_new_session=False, pass_fds=()):
        cmd = list(_CMD_DOCKER_EXEC) + args
        args = replace(cmd, _ID_CONTAINER_NAME, self.container_identifier)

        return super().Popen(args, bufsize, executable, stdin, stdout, stderr, preexec_fn, close_fds, shell, cwd, env,
                             universal_newlines, startupinfo, creationflags, restore_signals, start_new_session,
                             pass_fds)

    def container_is_created(self) -> bool:
        return container_is_created(
            container_identifier=self.container_identifier, command_processor=self._docker_host_command_processor)

    def container_delete(self):
        container_delete(
            container_identifier=self.container_identifier, command_processor=self._docker_host_command_processor)

    def container_create(self):
        container_create(container_identifier=self.container_identifier,
                         image_name=self.image_identifier, command_processor=self._docker_host_command_processor)

    def container_is_running(self) -> bool:
        return container_is_running(
            container_identifier=self.container_identifier, command_processor=self._docker_host_command_processor)

    def container_stop(self):
        container_stop(container_identifier=self.container_identifier,
                       command_processor=self._docker_host_command_processor)

    def container_start(self):
        container_start(container_identifier=self.container_identifier,
                        command_processor=self._docker_host_command_processor)
