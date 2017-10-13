#!/usr/bin/env python

import logging
import os
import shutil
import time
from threading import Lock

from pymmortals import threadprocessrouter as tpr
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.scenariorunnerconfiguration import JavaApplicationConfig
from pymmortals.scenariorunner.deploymentplatform import DeploymentPlatformInterface
from pymmortals.threadprocessrouter import ImmortalsSubprocess, global_subprocess
from pymmortals.utils import path_helper, replace

_ID_JAR_FILEPATH = '$JAR_FILEPATH!'

_CMD_START_JAR = ('java', '-jar', _ID_JAR_FILEPATH)


class JavaPlatform(DeploymentPlatformInterface):
    def application_destroy(self):
        # TODO: Implement
        pass

    def _is_ready(self):
        # Not necessary since it is the local system
        return True

    def _start(self):
        # Not necessary since it is the local system
        pass

    def clean(self):
        # TODO: Implement
        pass

    def _is_setup(self):
        # Not necessary since it is the local system
        return True

    def deploy_application(self, application_location):
        # Not necessary since it is currently run from the immortals tree
        pass

    def application_stop(self):
        with self._lock:
            self._application_process.terminate()

    def application_start(self):
        with self._lock:
            # TODO: This is not the server, it is a general Java app that is only used by the server...
            if get_configuration().validationEnvironment.startServer:
                with open(os.path.join(self.config.applicationDeploymentDirectory, 'stdout_log.txt'),
                          'w') as stdout_log, \
                        open(os.path.join(self.config.applicationDeploymentDirectory, 'stderr_log.txt'),
                             'w') as stderr_log:
                    logging.debug('Starting ' + self.jar_filepath + ' for ' + self.config.instanceIdentifier)
                    call_array = list(_CMD_START_JAR)
                    replace(call_array, _ID_JAR_FILEPATH, self.jar_filepath)
                    logging.debug('EXEC: ' + str(call_array))

                    self._application_process = tpr.global_subprocess.Popen(
                        args=call_array,
                        cwd=self.config.applicationDeploymentDirectory,
                        stdout=stdout_log,
                        stderr=stderr_log
                    )

                    # TODO: This really should be a configuration parameter...
                    time.sleep(2)

    def upload_file(self, source_file_location, file_target):
        with self._lock:
            logging.debug('Copying ' + source_file_location + ' to ' + file_target + '.')
            shutil.copyfile(source_file_location, file_target)

    def _destroy(self):
        with self._lock:
            self.clean()

    def _is_running(self):
        return True

    def __init__(self, application_configuration: JavaApplicationConfig,
                 command_processor: ImmortalsSubprocess = tpr.global_subprocess):

        if command_processor is None:
            command_processor = global_subprocess

        super().__init__(application_configuration=application_configuration, command_processor=command_processor)
        self.jar_filepath = path_helper(False, self.config.applicationDeploymentDirectory,
                                        os.path.basename(self.config.executableFile))

        self._application_process = None
        self._lock = Lock()

        if self.config.deploymentPlatformEnvironment == 'java_local':
            logging.info("Using local Java environment...")
        else:
            raise Exception("A valid java environment and identifier must be provided!")

    def setup(self):
        with self._lock:
            logging.debug(
                'Setting up ' + self.config.deploymentPlatformEnvironment + ' for ' + self.config.instanceIdentifier)
            logging.debug(
                'Setting up ' + self.config.deploymentPlatformEnvironment + ' for ' + self.config.instanceIdentifier)

            if get_configuration().validationEnvironment.setupEnvironmentLifecycle.destroyExisting:
                if os.path.exists(self.config.applicationDeploymentDirectory):
                    shutil.rmtree(self.config.applicationDeploymentDirectory)

            os.makedirs(self.config.applicationDeploymentDirectory)

    def _stop(self):
        # Not necessary since it is the local system:w
        pass
