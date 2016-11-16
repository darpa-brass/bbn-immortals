"""
Core android platform. Currently contains emulator usage that can be disabled
by overriding the relevent methods. Assumes ADB usage for deployment
"""

import atexit
import logging
import os
import subprocess
import time

import adbhelper
from configurationmanager import Configuration, AndroidApplicationConfig
import deploymentplatform
import emuhelper
from immortalsglobals import ImmortalsGlobals
from stdrouter import StdRouter
from utils import get_formatted_string_value

_halt_identifiers = []

_created_processes = []


def _exit_handler():
    logging.info('Cleaning up after androidplatform_emulator.py')
    for process in _created_processes:
        if not process is None and process.poll() is None:
            process.kill()


    # for identifier in exitidentifiers:
    #     call_list = [configurationmanager.ANDROID_BIN, 'list', 'avd']
    #     values = subprocess.check_output(call_list)
    #     if identifier in values:
    #
    #
    #         try:
    #             subprocess.call(['android', 'delete', 'avd', '--name', identifier], stdout=None, stderr=None)
    #         except:
    #             pass


atexit.register(_exit_handler)

_adb_has_been_reinitialized = False
_adb_identifier_count = 0
_emulator_name_template = 'emulator-{CONSOLEPORT}'
_port_base = 5560


def _generate_emulator_identifier():
    global _adb_identifier_count
    number = _adb_identifier_count
    _adb_identifier_count = _adb_identifier_count + 1

    int_identifier = None
    str_identifier = None
    if type(number) is int:
        int_identifier = number
        str_identifier = str(number)
    elif type(number) is str:
        int_identifier = str(number)
        str_identifier = number

    if int_identifier >= 22:
        raise Exception("No more than 22 devices are supported at this time!")

    elif len(str_identifier) == 1:
        str_identifier = '0' + str_identifier

    consoleport = _port_base + 2 * int_identifier
    return _emulator_name_template.format(CONSOLEPORT=consoleport)


class AndroidEmulatorInstance(deploymentplatform.DeploymentPlatform):
    """
    :type config: AndroidApplicationConfig
    """


    def __init__(self, application_configuration):
        self.config = application_configuration
        self.adb_device_identifier = _generate_emulator_identifier()
        # self.display_ui = Configuration.display_ui
        # self.execution_path = execution_path
        self.console_port = get_formatted_string_value(_emulator_name_template, self.adb_device_identifier,
                                                       'CONSOLEPORT')
        self.sdcard_filepath = None
        self.emulator_is_running = False
        self.is_application_running = False

        self.std_endpoint = StdRouter.get_endpoint(self.config.application_deployment_directory)

        self.emuhelper = emuhelper.EmuHelper(self.adb_device_identifier, self)
        self.adbhelper = adbhelper.AdbHelper(self.adb_device_identifier, self)

    def platform_setup(self):
        global _adb_has_been_reinitialized

        if not _adb_has_been_reinitialized:
            self.adbhelper.restart_adb_server()
            _adb_has_been_reinitialized = True

        logging.debug('Setting up ' + self.config.deployment_platform_environment + ' for ' + self.config.instance_identifier)

        is_known = self.adbhelper.is_known()
        is_running = self._emulator_exists()
        does_exist = self.emuhelper.emulator_exists()
        console_port = get_formatted_string_value(_emulator_name_template, self.adb_device_identifier, 'CONSOLEPORT')

        if ImmortalsGlobals.wipe_existing_environment:
            if is_running:
                self._kill_emulator()
                is_running = False

            if does_exist:
                self._delete_emulator()
                self.is_known = False

        if not ImmortalsGlobals.keep_constructed_environment_running:
            _halt_identifiers.append(self.adb_device_identifier)

        sdcard_filepath = os.path.join(self.config.application_deployment_directory, self.config.instance_identifier + '_sdcard.img')
        cmd = ['mksdcard', '12M', sdcard_filepath]
        self.call(cmd)
        self.sdcard_filepath = sdcard_filepath

        if not is_known:
            self._create_emulator()

        if not is_running:
            console_port = get_formatted_string_value(_emulator_name_template, self.adb_device_identifier,
                                                      'CONSOLEPORT')
            self._start_emulator(console_port, ImmortalsGlobals.display_guis, self.sdcard_filepath)

        self.adbhelper.wait_for_device_ready()
        self.emulator_is_running = True

    def deploy_application(self, application_location):
        return self.adbhelper.deploy_apk(application_location)

    def upload_file(self, source_file_location, file_target):
        return self.adbhelper.upload_file(source_file_location, file_target)

    def start_application(self):
        self.adbhelper.start_process(self.config.package_identifier + '/' + self.config.main_activity)
        self.is_application_running = True

        time.sleep(2)

    def stop_application(self):
        self.adbhelper.force_stop_process(self.config.package_identifier)
        self.is_application_running = False

    def platform_teardown(self):
        if self.adb_device_identifier in _halt_identifiers:
            self._kill_emulator()
            self.emulator_is_running = False

#        if self.adb_device_identifier in _remove_identifiers:
#            self._delete_emulator()

    def call(self, call_list):
        logging.debug('EXEC: ' + str(call_list))

        return subprocess.call(call_list, stdout=self.std_endpoint.out, stderr=self.std_endpoint.err)

    def check_output(self, call_list):
        logging.debug('EXEC: ' + str(call_list))
        return subprocess.check_output(call_list)

    def Popen(self, args, bufsize=0, executable=None, stdin=None, stdout=None, stderr=None, preexec_fn=None,
              close_fds=False, shell=False, cwd=None, env=None, universal_newlines=False, startupinfo=None,
              creationflags=0):
        logging.debug('EXEC: ' + str(args))
        if stdout is None:
            stdout = self.std_endpoint.out

        if stderr is None:
            stderr = self.std_endpoint.err

        process = subprocess.Popen(args, bufsize, executable, stdin, stdout, stderr, preexec_fn, close_fds, shell, cwd,
                                   env, universal_newlines, startupinfo, creationflags)
        _created_processes.append(process)
        return process

    # def _monitor_output(self, event_listener):
    #     is_shutdown = False
    #     while self.is_application_running and not is_shutdown:
    #         line = self.logcat_process.stdout.readline().strip('\n')
    #
    #         try:
    #             value = commentjson.loads(line)
    #             if (value['type'] == 'ClientShutdown' or not self.logcat_process.poll() is None):
    #                 is_shutdown = True
    #
    #             event_listener(self.config.instance_identifier, value)
    #
    #         except JSONLibraryException:
    #             if is_shutdown:
    #                 # If it has shut down we don't care if this freaks out
    #                 pass
    #
    #     self.platform_teardown()


    def _start_emulator(self, console_port, display_ui, sdcard_filepath):
        self.emuhelper.start_emulator(console_port, display_ui, sdcard_filepath)

    def _kill_emulator(self):
        self.emuhelper.kill_emulator()

    def _delete_emulator(self):
        self.emuhelper.delete_emulator()

    def _create_emulator(self):
        self.emuhelper.create_emulator()

    def _emulator_exists(self):
        return self.emuhelper.emulator_exists()
