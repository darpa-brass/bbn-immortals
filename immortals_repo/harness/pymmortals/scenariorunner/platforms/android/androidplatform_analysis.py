#!/usr/bin/env python

import os

from pymmortals.datatypes.scenariorunnerconfiguration import AndroidApplicationConfig
from pymmortals.scenariorunner.dockerhelper import DockerHelper
from pymmortals.threadprocessrouter import ImmortalsSubprocess, global_subprocess
from ...deploymentplatform import DeploymentPlatformInterface

_CONTAINER_NAME = os.environ['USER'] + '-analysis'


class AndroidStaticAnalysisInstance(DeploymentPlatformInterface):
    def __init__(self, application_configuration: AndroidApplicationConfig,
                 command_processor: ImmortalsSubprocess = None):
        self._docker_helper: DockerHelper = DockerHelper(
            container_identifier=_CONTAINER_NAME,
            image_identifier='immortals/ucr_analysis',
            log_tag=_CONTAINER_NAME,
            command_processor=global_subprocess if command_processor is None else command_processor
        )

        super().__init__(application_configuration, self._docker_helper)

    def _is_running(self) -> bool:
        return self._docker_helper.container_is_running()

    def _start(self):
        self._docker_helper.container_start()

    def _stop(self):
        self._docker_helper.container_stop()

    def _is_ready(self) -> bool:
        return self._is_running()

    def _destroy(self):
        self._docker_helper.container_delete()

    def upload_file(self, source_file_location: str, file_target: str):
        self._docker_helper.
        pass

    def clean(self):
        pass

    def application_destroy(self):
        pass

    def deploy_application(self, application_location: str):
        pass

    def application_stop(self):
        pass

    def _is_setup(self) -> bool:
        return self._docker_helper.container_is_created()

    def setup(self):
        self._docker_helper.container_create()

    def application_start(self):
        pass

        # def __init__(self, application_configuration):
        #     self.config = application_configuration
        #     self.docker = DockerInstance(self.config.applicationDeploymentDirectory, application_configuration)
        # 
        # def setup(self):
        #     self.docker.setup()
        # 
        # def deploy_application(self, application_location):
        #     self.apk_filename = os.path.basename(application_location)
        #     target_filepath = os.path.join('/bbnAnalysis/apks/', self.apk_filename)
        #     self.docker.copy_file_to_docker(application_location, target_filepath)
        # 
        # def upload_file(self, source_file_location, file_target):
        #     self.docker.upload_file(source_file_location, file_target)
        # 
        # def application_start(self):
        #     file_found = False
        #     for filepath in self.docker.files:
        #         if os.path.basename(filepath) == 'android_staticanalysis.sh':
        #             file_found = True
        #             cmd = ['bash', filepath, self.apk_filename]
        # 
        #             logging.info(
        #                 "Performing static analysis on '" + self.apk_filename + "'.  Please be patient as this will take several minutes...")
        #             self.docker.call(cmd)
        # 
        #             logging.info("Static analysis complete.")
        # 
        #             basename = os.path.splitext(os.path.basename(self.apk_filename))[0]
        #             self.docker.copy_file_from_docker('/bbnAnalysis/output/' + basename + 'dir.log',
        #                                               self.config.applicationDeploymentDirectory)
        #             self.docker.copy_file_from_docker('/bbnAnalysis/output/' + basename + 'dir.output',
        #                                               self.config.applicationDeploymentDirectory)
        #             self.docker.copy_file_from_docker('/bbnAnalysis/output/' + basename + 'dir.ddg.global.dot',
        #                                               self.config.applicationDeploymentDirectory)
        # 
        #     if not file_found:
        #         raise Exception("No file has been found in the droidscope instance for android_staticanalysis.sh!!!")
        # 
        # def application_stop(self):
        #     self.docker.application_stop()
        # 
        # def stop(self):
        #     self.docker.stop()
