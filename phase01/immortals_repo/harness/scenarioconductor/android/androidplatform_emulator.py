"""
Core android platform. Currently contains emulator usage that can be disabled
by overriding the relevant methods. Assumes ADB usage for deployment
"""
import logging
import os
import time
from threading import Lock

import adbhelper
import emuhelper
from .. import immortalsglobals as ig
from .. import threadprocessrouter as tpr
from ..data.applicationconfig import AndroidApplicationConfig
from ..deploymentplatform import DeploymentPlatformInterface
from ..interfaces import CommandHandlerInterface
from ..utils import get_formatted_string_value, log_time_delta_0, log_time_delta_1

_adb_has_been_reinitialized = False
_global_lock = Lock()
_adb_identifier_count = 0


class AndroidEmulatorInstance(DeploymentPlatformInterface):
    """
    :type config: AndroidApplicationConfig
    :type command_processor: CommandHandlerInterface
    """

    def __init__(self, application_configuration, command_processor=tpr):

        self.config = application_configuration
        self.std_endpoint = None

        DeploymentPlatformInterface.__init__(self,
                                             command_processor=command_processor,
                                             halt_on_shutdown=ig.configuration.validationEnvironment.lifecycle.haltEnvironment
                                             )

        self.adb_device_identifier = emuhelper.generate_emulator_identifier()
        self.console_port = int(get_formatted_string_value(emuhelper.emulator_name_template, self.adb_device_identifier,
                                                           'CONSOLEPORT'))
        self.adb_port = self.console_port + 1
        self.sdcard_filepath = os.path.join(self.config.applicationDeploymentDirectory,
                                            self.config.instanceIdentifier + '_sdcard.img')
        self.emulator_is_running = False
        self.is_application_running = False

        self.adbhelper = adbhelper.AdbHelper(application_configuration, self.adb_device_identifier, command_processor)
        self.emuhelper = emuhelper.EmuHelper(self.adb_device_identifier, self.console_port,
                                             self.sdcard_filepath, command_processor)

    def setup(self):
        global _global_lock, _adb_has_been_reinitialized

        with _global_lock:
            if not _adb_has_been_reinitialized:
                self.adbhelper.restart_adb_server()
                _adb_has_been_reinitialized = True

        self.std_endpoint = tpr.get_std_endpoint(self.config.applicationDeploymentDirectory)
        self.stdout = self.std_endpoint.out
        self.stderr = self.std_endpoint.err

        logging.debug(
            'Setting up ' + self.config.deploymentPlatformEnvironment + ' for ' + self.config.instanceIdentifier)

        cmd = ['mksdcard', '12M', self.sdcard_filepath]
        self.call(cmd)

        self._create_emulator()

    def _destroy(self):
        is_running = self.adbhelper.is_known()
        does_exist = self._emulator_exists()

        if is_running:
            self._kill_emulator()
            is_running = False

        if does_exist:
            self._delete_emulator()
            is_known = False

    def deploy_application(self, application_location):
        if ig.configuration.debugMode:
            log_time_delta_0(self.config.instanceIdentifier, 'deploy_application')
            val = self.adbhelper.deploy_apk(application_location)
            log_time_delta_1(self.config.instanceIdentifier, 'deploy_application')
            return val
        else:
            return self.adbhelper.deploy_apk(application_location)

    def upload_file(self, source_file_location, file_target):
        if ig.configuration.debugMode:
            log_time_delta_0(self.config.instanceIdentifier, 'upload_file')
            val = self.adbhelper.upload_file(source_file_location, file_target)
            log_time_delta_1(self.config.instanceIdentifier, 'upload_file')
            return val
        else:
            return self.adbhelper.upload_file(source_file_location, file_target)

    def application_start(self):
        if ig.configuration.debugMode:
            log_time_delta_0(self.config.instanceIdentifier, 'application_start')
            self.adbhelper.start_process()
            log_time_delta_1(self.config.instanceIdentifier, 'application_start')
        else:
            self.adbhelper.start_process()

        self.is_application_running = True
        time.sleep(2)

    def application_stop(self):
        if ig.configuration.debugMode:
            log_time_delta_0(self.config.instanceIdentifier, 'application_stop')
            self.adbhelper.force_stop_process()
            log_time_delta_1(self.config.instanceIdentifier, 'application_stop')
        else:
            self.adbhelper.force_stop_process()

        self.is_application_running = False

    def _stop(self):
        if ig.configuration.debugMode:
            log_time_delta_0(self.config.instanceIdentifier, '_stop')
            self._kill_emulator()
            log_time_delta_1(self.config.instanceIdentifier, '_stop')
        else:
            self._kill_emulator()

        self.emulator_is_running = False

    def _start_emulator(self):
        if ig.configuration.debugMode:
            log_time_delta_0(self.config.instanceIdentifier, '_start_emulator')
            self.emuhelper.start_emulator()
            log_time_delta_1(self.config.instanceIdentifier, '_start_emulator')
        else:
            self.emuhelper.start_emulator()

    def _kill_emulator(self):
        if ig.configuration.debugMode:
            log_time_delta_0(self.config.instanceIdentifier, '_kill_emulator')
            self.emuhelper.kill_emulator()
            log_time_delta_1(self.config.instanceIdentifier, '_kill_emulator')
        else:
            self.emuhelper.kill_emulator()

        self.emulator_is_running = False

    def _delete_emulator(self):
        if ig.configuration.debugMode:
            log_time_delta_0(self.config.instanceIdentifier, '_delete_emulator')
            self.emuhelper.delete_emulator()
            log_time_delta_1(self.config.instanceIdentifier, '_delete_emulator')
        else:
            self.emuhelper.delete_emulator()

    def _create_emulator(self):
        if ig.configuration.debugMode:
            log_time_delta_0(self.config.instanceIdentifier, '_create_emulator')
            self.emuhelper.create_emulator()
            log_time_delta_1(self.config.instanceIdentifier, '_create_emulator')
        else:
            self.emuhelper.create_emulator()

    def _emulator_exists(self):
        return self.emuhelper.emulator_exists()

    def _is_running(self):
        return self.adbhelper.is_known()

    def _is_setup(self):
        return self._emulator_exists()

    def _is_ready(self):
        return self.adbhelper.is_fully_booted()

    def clean(self):
        self.application_destroy()

    def _start(self):
        self._start_emulator()

    def application_destroy(self):
        if ig.configuration.debugMode:
            log_time_delta_0(self.config.instanceIdentifier, 'application_destroy')
            self.adbhelper.uninstall_package()
            for f in self.config.filesForCleanup:
                self.adbhelper.remove_file_recursively(f)
            log_time_delta_1(self.config.instanceIdentifier, 'application_destroy')
        else:
            self.adbhelper.uninstall_package()
            for f in self.config.filesForCleanup:
                self.adbhelper.remove_file_recursively(f)
