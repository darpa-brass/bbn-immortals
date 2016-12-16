"""
An android-specific docker platform.  Parts could probably be abstracted out
into a separate docker platform for java if necessary later on.
"""

import androidplatform_emulator
from .. import docker
from ..data.applicationconfig import AndroidApplicationConfig
from ..deploymentplatform import DeploymentPlatformInterface

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


class AndroidDockerEmulatorInstance(DeploymentPlatformInterface):
    """
    :type config: AndroidApplicationConfig
    """

    def __init__(self, application_configuration):
        self.config = application_configuration
        self.docker = docker.DockerInstance(self.config.applicationDeploymentDirectory, application_configuration,
                                            True)
        self.emulator = androidplatform_emulator.AndroidEmulatorInstance(application_configuration)
        self.emulator.call = self.docker.call
        self.emulator.Popen = self.docker.Popen
        self.emulator.check_output = self.docker.check_output

    def setup(self):
        self.docker.setup()
        self.emulator.setup()

    def deploy_application(self, application_location):
        self.docker.deploy_application(application_location)
        self.emulator.deploy_application(application_location)

    def upload_file(self, source_file_location, file_target):
        self.docker.upload_file(source_file_location, file_target)
        self.emulator.upload_file(source_file_location, file_target)

    def application_start(self):
        self.docker.application_start()
        self.emulator.application_start()

    def application_stop(self):
        self.emulator.application_stop()
        self.docker.application_stop()

    def stop(self):
        self.emulator.stop()
        self.emulator.stop()
