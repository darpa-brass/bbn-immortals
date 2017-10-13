# """
# An android-specific docker platform.  Parts could probably be abstracted out
# into a separate docker platform for java if necessary later on.
# """
# import logging
# import shutil
# import uuid
# from threading import Lock
# 
# import os
# 
# from pymmortals.datatypes.root_configuration import get_configuration
# from pymmortals.datatypes.scenariorunnerconfiguration import ApplicationConfig
# from pymmortals.interfaces import CommandHandlerInterface
# from pymmortals.scenariorunner.deploymentplatform import DeploymentPlatformInterface
# from pymmortals.scenariorunner.dockerhelper import DockerHelper
# from pymmortals.utils import path_helper, replace
# 
# # _halt_identifiers = []
# # _remove_identifiers = []
# 
# _ID_SHARED = '$SHARED!'
# _ID_DS_SHARED = '$DS_SHARED!'
# _ID_SOURCE_FILE = '$SOURCE_FILE!'
# _ID_TARGET_FILE = '$TARGET_FILE!'
# ID_CONTAINER_NAME = '$CONTAINER_NAME!'
# _ID_IMAGE_IDENTIFIER = '$IMAGE_IDENTIFIER!'
# 
# _CMD_DOCKER_EXEC = ('sudo', 'docker', 'exec', ID_CONTAINER_NAME)
# # _CMD_GET_DOCKER_CREATED_CONTAINER_IDENTIFIER = (
# #     'sudo', 'docker', 'ps', '-a', '-q', '--filter=name=' + ID_CONTAINER_NAME)
# # _CMD_GET_DOCKER_RUNNING_CONTAINER_IDENTIFIER = ('sudo', 'docker', 'ps', '-q', '--filter=name=' + ID_CONTAINER_NAME)
# # _CMD_CREATE_CONTAINER = (
# #     'sudo', 'docker', 'run', '-itd', '--device=/dev/kvm', '--name=' + ID_CONTAINER_NAME, _ID_IMAGE_IDENTIFIER)
# # 
# # _CMD_START_CONTAINER = ('sudo', 'docker', 'start', ID_CONTAINER_NAME)
# # _CMD_STOP_CONTAINER = ('sudo', 'docker', 'stop', ID_CONTAINER_NAME)
# # _CMD_DELETE_CONTAINER = ('sudo', 'docker', 'rm', ID_CONTAINER_NAME)
# _CMD_COPY_TO_DOCKER = ('sudo', 'docker', 'cp', _ID_SOURCE_FILE, ID_CONTAINER_NAME + ':' + _ID_TARGET_FILE)
# 
# 
# class DockerInstance(DeploymentPlatformInterface):
#     # 
#     # def application_stop(self):
#     #     raise NotImplementedError
#     # 
#     # def application_start(self):
#     #     raise NotImplementedError
#     # 
#     # def __init__(self, application_configuration: ApplicationConfig, command_processor=None):
#     #     DeploymentPlatformInterface.__init__(self, command_processor=command_processor)
#     # 
#     #     self.docker_helper = DockerHelper(
#     #         container_identifier=application_configuration.instanceIdentifier,
#     #         image_identifier=application_configuration.deploymentPlatformEnvironment,
#     #         command_processor=None
#     #     )
#     # 
#     #     self.config = application_configuration
#     #     self.lock = Lock()
#     # 
#     # def setup(self):
#     #     container_exists = self.docker_helper.container_is_created()
#     #     is_running = self.docker_helper.container_is_running()
#     #     
#     #     
#     #     existing_id = self.docker_helper.container_is_created()
#     # 
#     #     if is_running:
#     #         if get_configuration().validationEnvironment.setupEnvironmentLifecycle.destroyExisting:
#     #             _halt_identifiers.append(self.config.instanceIdentifier)
#     #             self.docker_helper.container_stop()
#     #     else:
#     #         _halt_identifiers.append(self.config.instanceIdentifier)
#     # 
#     #     if existing_id:
#     #         if get_configuration().validationEnvironment.setupEnvironmentLifecycle.destroyExisting:
#     #             _remove_identifiers.append(self.config.instanceIdentifier)
#     #             self.docker_helper.container_delete()
#     #     else:
#     #         _remove_identifiers.append(self.config.instanceIdentifier)
#     # 
#     #     files = []
#     # 
#     #     if not existing_id or get_configuration().validationEnvironment.setupEnvironmentLifecycle.destroyExisting:
#     #         self.docker_helper.container_create()
#     # 
#     #     # If it is not running, start it
#     #     self.docker_helper.container_start()
#     # 
#     #     cmd = ['mkdir', '-p', self.config.applicationDeploymentDirectory]
#     #     self.check_output(cmd)
#     # 
#     #     for script in get_configuration().scenarioRunner.docker.scripts:
#     #         filepath = path_helper(True, get_configuration().immortalsRoot, script)
#     #         files.append(filepath)
#     #         self.copy_file_to_docker(filepath, filepath)
#     # 
#     # def deploy_application(self, apk_location):
#     #     self.copy_file_to_docker(apk_location, apk_location)
#     # 
#     # def upload_file(self, source_file_location, file_target):
#     #     self.copy_file_to_docker(source_file_location, source_file_location)
#     # 
#     # def stop(self):
#     #     if self.config.instanceIdentifier in _halt_identifiers:
#     #         self.docker_helper.container_stop()
#     # 
#     #         # Not doing since it makes analysis of issues impossible
#     #         # if self.config.instance_identifier in _remove_identifiers:
#     #         # self._remove_docker_container()
#     # 
#     # def call(self, args, stdout=None, stderr=None, *popenargs, **kwargs):
#     # 
#     #     cmd = list(_CMD_DOCKER_EXEC) + args
#     #     args = replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)
#     # 
#     #     return CommandHandlerInterface.call(self, args, stdout, stderr, *popenargs, **kwargs)
#     # 
#     # def check_call(self, args, stdout=None, stderr=None, *popenargs, **kwargs):
#     # 
#     #     cmd = list(_CMD_DOCKER_EXEC) + args
#     #     args = replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)
#     #     return CommandHandlerInterface.check_call(self, args, stdout, stderr, *popenargs, **kwargs)
#     # 
#     # def check_output(self, args, stderr=None, *popenargs, **kwargs):
#     # 
#     #     cmd = list(_CMD_DOCKER_EXEC) + args
#     #     args = replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)
#     #     return CommandHandlerInterface.check_output(self, args, stderr, *popenargs, **kwargs)
#     # 
#     # def Popen(self, args, stdout=None, stderr=None, *popenargs, **kwargs):
#     # 
#     #     cmd = list(_CMD_DOCKER_EXEC) + args
#     #     args = replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)
#     #     return CommandHandlerInterface.Popen(self, args, stdout, stderr, *popenargs, **kwargs)
#     # 
#     # def copy_file_to_docker(self, local_filepath, target_filepath):
#     # 
#     #     cmd = ['mkdir', '-p', os.path.dirname(target_filepath)]
#     #     self.check_output(cmd)
#     # 
#     #     cmd = list(_CMD_COPY_TO_DOCKER)
#     #     cmd = replace(cmd, ID_CONTAINER_NAME, self.config.instanceIdentifier)
#     #     cmd = replace(cmd, _ID_SOURCE_FILE, local_filepath)
#     #     cmd = replace(cmd, _ID_TARGET_FILE, target_filepath)
#     #     self.call(cmd)
#     # 
#     # def copy_file_from_docker(self, docker_filepath, target_directory):
#     #     filename = os.path.basename(docker_filepath)
#     #     tmp_target = os.path.join('/tmp/', str(uuid.uuid4()))
#     #     final_target = os.path.join(target_directory, filename)
#     # 
#     #     cmd = ['sudo', 'docker', 'cp', self.config.instanceIdentifier + ':' + docker_filepath, tmp_target]
#     #     self.call(cmd)
#     # 
#     #     shutil.copyfile(tmp_target, final_target)
#     # 
#     #     cmd = ['sudo', 'rm', tmp_target]
#     #     self.call(cmd)
