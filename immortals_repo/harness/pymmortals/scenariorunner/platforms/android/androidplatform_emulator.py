"""
Core android platform. Currently contains emulator usage that can be disabled
by overriding the relevant methods. Assumes ADB usage for deployment
"""

import logging
import os
import time
from threading import Lock

from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.scenariorunnerconfiguration import AndroidApplicationConfig
from pymmortals.immortalsglobals import get_event_router
from pymmortals.scenariorunner.deploymentplatform import DeploymentPlatformInterface
from pymmortals.threadprocessrouter import ImmortalsSubprocess, global_subprocess
from pymmortals.utils import get_formatted_string_value
from . import adbhelper, emuhelper

_adb_has_been_reinitialized = False
_global_lock = Lock()


class AndroidEmulatorInstance(DeploymentPlatformInterface):
    def __init__(self, application_configuration: AndroidApplicationConfig,
                 command_processor: ImmortalsSubprocess = None):

        if command_processor is None:
            command_processor = global_subprocess

        super().__init__(application_configuration=application_configuration, command_processor=command_processor)
        self.std_endpoint = None

        self.adb_device_identifier = emuhelper.generate_emulator_identifier()
        self.console_port = int(get_formatted_string_value(emuhelper.emulator_name_template, self.adb_device_identifier,
                                                           'CONSOLEPORT'))
        self.adb_port = self.console_port + 1
        self.sdcard_filepath = os.path.join(self.config.applicationDeploymentDirectory,
                                            self.config.instanceIdentifier + '_sdcard.img')
        self.emulator_is_running = False
        self.is_application_running = False

        self.adbhelper = adbhelper.AdbHelper(application_configuration, self.adb_device_identifier, command_processor)
        self.emuhelper = emuhelper.EmuHelper(
            adb_device_identifier=self.adb_device_identifier,
            console_port=self.console_port,
            command_processor=self,
            instance_identifier=self.config.instanceIdentifier)

    def setup(self):
        global _global_lock, _adb_has_been_reinitialized

        with _global_lock:
            if not _adb_has_been_reinitialized:
                self.adbhelper.restart_adb_server()
                _adb_has_been_reinitialized = True

        logging.debug(
            'Setting up ' + self.config.deploymentPlatformEnvironment + ' for ' + self.config.instanceIdentifier)

        cmd = ['mksdcard', '12M', self.sdcard_filepath]
        self.run(cmd)

        self._create_emulator()

    def _destroy(self):
        is_running = self.adbhelper.is_known()
        does_exist = self._emulator_exists()

        if is_running:
            self._kill_emulator()

        if does_exist:
            self._delete_emulator()

    def deploy_application(self, application_location):
        """
        :type application_location: str
        """
        if get_configuration().debugMode:
            get_event_router().log_time_delta_0(self.config.instanceIdentifier, 'deploy_application')
            val = self.adbhelper.deploy_apk(application_location)
            get_event_router().log_time_delta_1(self.config.instanceIdentifier, 'deploy_application')
            return val
        else:
            return self.adbhelper.deploy_apk(application_location)

    def upload_file(self, source_file_location, file_target):
        """
        :type source_file_location: str
        :type file_target: str
        """
        if get_configuration().debugMode:
            get_event_router().log_time_delta_0(self.config.instanceIdentifier, 'upload_file')
            val = self.adbhelper.upload_file(source_file_location, file_target)
            get_event_router().log_time_delta_1(self.config.instanceIdentifier, 'upload_file')
            return val
        else:
            return self.adbhelper.upload_file(source_file_location, file_target)

    def application_start(self):
        if get_configuration().debugMode:
            get_event_router().log_time_delta_0(self.config.instanceIdentifier, 'application_start')
            self.adbhelper.start_process()
            get_event_router().log_time_delta_1(self.config.instanceIdentifier, 'application_start')
        else:
            self.adbhelper.start_process()

        self.is_application_running = True
        time.sleep(2)

    def application_stop(self):
        if get_configuration().debugMode:
            get_event_router().log_time_delta_0(self.config.instanceIdentifier, 'application_stop')
            self.adbhelper.force_stop_process()
            get_event_router().log_time_delta_1(self.config.instanceIdentifier, 'application_stop')
        else:
            self.adbhelper.force_stop_process()

        self.is_application_running = False

    def _stop(self):
        if get_configuration().debugMode:
            get_event_router().log_time_delta_0(self.config.instanceIdentifier, '_stop')
            self._kill_emulator()
            get_event_router().log_time_delta_1(self.config.instanceIdentifier, '_stop')
        else:
            self._kill_emulator()

        self.emulator_is_running = False

    def _start_emulator(self):
        if get_configuration().debugMode:
            get_event_router().log_time_delta_0(self.config.instanceIdentifier, '_start_emulator')
            self.emuhelper.start_emulator()
            get_event_router().log_time_delta_1(self.config.instanceIdentifier, '_start_emulator')
        else:
            self.emuhelper.start_emulator()

    def _kill_emulator(self):
        if get_configuration().debugMode:
            get_event_router().log_time_delta_0(self.config.instanceIdentifier, '_kill_emulator')
            self.emuhelper.kill_emulator()
            get_event_router().log_time_delta_1(self.config.instanceIdentifier, '_kill_emulator')
        else:
            self.emuhelper.kill_emulator()

        self.emulator_is_running = False

    def _delete_emulator(self):
        if get_configuration().debugMode:
            get_event_router().log_time_delta_0(self.config.instanceIdentifier, '_delete_emulator')
            self.emuhelper.delete_emulator()
            get_event_router().log_time_delta_1(self.config.instanceIdentifier, '_delete_emulator')
        else:
            self.emuhelper.delete_emulator()

    def _create_emulator(self):
        if get_configuration().debugMode:
            get_event_router().log_time_delta_0(self.config.instanceIdentifier, '_create_emulator')
            self.emuhelper.create_emulator()
            get_event_router().log_time_delta_1(self.config.instanceIdentifier, '_create_emulator')
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
        if get_configuration().debugMode:
            get_event_router().log_time_delta_0(self.config.instanceIdentifier, 'application_destroy')
            self.adbhelper.uninstall_package()
            for f in self.config.filesForCleanup:
                self.adbhelper.remove_file_recursively(f)
            get_event_router().log_time_delta_1(self.config.instanceIdentifier, 'application_destroy')
        else:
            self.adbhelper.uninstall_package()
            for f in self.config.filesForCleanup:
                self.adbhelper.remove_file_recursively(f)
