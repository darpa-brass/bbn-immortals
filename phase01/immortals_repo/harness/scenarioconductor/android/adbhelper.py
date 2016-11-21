"""
This class contains functions for interacting with ADB
"""

import atexit
import logging
import subprocess
import sys
import time

from utils import replace

ADB_BIN = 'adb'

_ID_ADB_DEVICE_IDENTIFIER = '$ADB_DEVICE_IDENTIFIER!'
_ID_PACKAGE_ACTIVITY = '$PACKAGE_ACTIVITY!'
_ID_PACKAGE = '$PACKAGE!'
_ID_PERMISSION = '$PERMISSION!'
_ID_SOURCE_FILEPATH = '$SOURCE_FILEPATH!'
_ID_TARGET_FILEPATH = '$TARGET_FILEPATH!'
_ID_TAG = '$TAG!'

_adb_process_start_command = (
ADB_BIN, '-s', _ID_ADB_DEVICE_IDENTIFIER, 'shell', 'am', 'start', '-n', _ID_PACKAGE_ACTIVITY)
_adb_process_stop_command = (ADB_BIN, '-s', _ID_ADB_DEVICE_IDENTIFIER, 'shell', 'am', 'force-stop', _ID_PACKAGE)
_adb_grant_permission_command = (
ADB_BIN, '-s', _ID_ADB_DEVICE_IDENTIFIER, 'shell', 'pm', 'grant', _ID_PACKAGE, _ID_PERMISSION)
_adb_upload_command = (ADB_BIN, '-s', _ID_ADB_DEVICE_IDENTIFIER, 'push', _ID_SOURCE_FILEPATH, _ID_TARGET_FILEPATH)
_adb_deploy_apk_command = (ADB_BIN, '-s', _ID_ADB_DEVICE_IDENTIFIER, 'install', '-r', _ID_SOURCE_FILEPATH)
_adb_list_devices = (ADB_BIN, 'devices')
_adb_get_device_status_command = (ADB_BIN, '-s', _ID_ADB_DEVICE_IDENTIFIER, 'shell', 'getprop', 'init.svc.bootanim')
_adb_kill_server = (ADB_BIN, 'kill-server')
_adb_start_server = (ADB_BIN, 'start-server')
_adb_logcat_with_tag = (ADB_BIN, '-s', _ID_ADB_DEVICE_IDENTIFIER, 'logcat', '-v', 'raw', '-b', 'main', '-s', _ID_TAG)
_adb_logcat_flush_with_tag = (
ADB_BIN, '-s', _ID_ADB_DEVICE_IDENTIFIER, 'logcat', '-v', 'raw', '-b', 'main', '-c', '-s', _ID_TAG)
_adb_unlock_device_nopassword = (ADB_BIN, '-s', _ID_ADB_DEVICE_IDENTIFIER, 'shell', 'input', 'keyevent', '82')

_created_processes = []


def _exit_handler():
    for process in _created_processes:
        if not process is None:
            process.kill()


atexit.register(_exit_handler)


class AdbHelper:
    def __init__(self, adb_device_identifier, command_processor=None):
        self.adb_device_identifier = adb_device_identifier

        if command_processor is None:
            self.command_processor = self
        else:
            self.command_processor = command_processor

    def call(self, call_list):
        logging.debug('EXEC: ' + call_list)
        return subprocess.call(call_list)

    def check_output(self, call_list):
        logging.debug('EXEC: ' + call_list)
        return subprocess.check_output(call_list)

    def Popen(self, args, bufsize=0, executable=None, stdin=None, stdout=None, stderr=None, preexec_fn=None,
              close_fds=False, shell=False, cwd=None, env=None, universal_newlines=False, startupinfo=None,
              creationflags=0):
        logging.debug('EXEC: ' + args)
        process = subprocess.Popen(args, bufsize, executable, stdin, stdout, stderr, preexec_fn, close_fds, shell, cwd,
                                   env, universal_newlines, startupinfo, creationflags)
        _created_processes.append(process)
        return process

    """
    Uploads a file to the device specified by adb_device_identifier.
    source_file_location specifies the location of the source file, while
    file_target specifies the new name of the file that will be placed in '/sdcard/'
    """

    def upload_file(self, source_file_location, file_target):
        call_array = list(_adb_upload_command)
        replace(call_array, _ID_ADB_DEVICE_IDENTIFIER, self.adb_device_identifier)
        replace(call_array, _ID_SOURCE_FILEPATH, source_file_location)
        replace(call_array, _ID_TARGET_FILEPATH, file_target)
        self.command_processor.call(call_array)

    """
    Grants provided permission_strings to the provided package on the device
    specified by adb_device_identifier
    """

    def grant_permission(self, package, permission_string):
        call_array = list(_adb_grant_permission_command)
        replace(call_array, _ID_ADB_DEVICE_IDENTIFIER, self.adb_device_identifier)
        replace(call_array, _ID_PACKAGE, package)
        replace(call_array, _ID_PERMISSION, permission_string)
        self.command_processor.call(call_array)

    """
    Starts package_activity_identifier on adb_device_identifier
    """

    def start_process(self, package_activity_identifier):
        call_array = list(_adb_process_start_command)
        replace(call_array, _ID_ADB_DEVICE_IDENTIFIER, self.adb_device_identifier)
        replace(call_array, _ID_PACKAGE_ACTIVITY, package_activity_identifier)
        self.command_processor.call(call_array)

    """
    Performs a forceful stop of all activities and services provided by
    package_identifier on adb_device_identifier
    """

    def force_stop_process(self, package_identifier):
        call_array = list(_adb_process_stop_command)
        replace(call_array, _ID_ADB_DEVICE_IDENTIFIER, self.adb_device_identifier)
        replace(call_array, _ID_PACKAGE, package_identifier)
        self.command_processor.call(call_array)

    """
    Deploys the apk specified by apk_location on adb_device_identifier, removing
    the current version on the device if it exists
    """

    def deploy_apk(self, apk_location):
        call_array = list(_adb_deploy_apk_command)
        replace(call_array, _ID_ADB_DEVICE_IDENTIFIER, self.adb_device_identifier)
        replace(call_array, _ID_SOURCE_FILEPATH, apk_location)
        self.command_processor.call(call_array)

    """
    Indicates if the device provider specified by adb_device_identifier is
    known by adb. Note that just because it is known doesn't mean it is connected,
    online, or finished booting
    """

    def is_known(self):
        list_devices_call_array = list(_adb_list_devices)
        list_devices_result = self.command_processor.check_output(list_devices_call_array)
        search_string = self.adb_device_identifier + '\tdevice'
        return list_devices_result.find(search_string) >= 0

    """
    Unlocks a device provided it does not have a password
    """

    def unlock_device_nopassword(self):
        call_array = list(_adb_unlock_device_nopassword)
        replace(call_array, _ID_ADB_DEVICE_IDENTIFIER, self.adb_device_identifier)
        self.command_processor.call(call_array)

    """
    Indicates if a device has finished booting judging by the state of the boot
    screen.  If the boot screen is disabled, this will not work!
    """

    def is_fully_booted(self):
        if self.is_known():
            call_array = list(_adb_get_device_status_command)
            replace(call_array, _ID_ADB_DEVICE_IDENTIFIER, self.adb_device_identifier)
            result = self.command_processor.check_output(call_array).strip()
            return result == 'stopped'

        return False

    """
    Returns an adb logcat process instance on the device indicated by
    adb_device_identifier filtering by logcat_tag.  If flush_first is True,
    the existing logcat messages will be flushed before the returned logcat
    process is executed.
    """
    # def logcat_with_tag(self, flush_first, logcat_tag):
    #     if flush_first:
    #         flush_call_array = list(_adb_logcat_flush_with_tag)
    #         replace(flush_call_array, _ID_ADB_DEVICE_IDENTIFIER, self.adb_device_identifier)
    #         replace(flush_call_array, _ID_TAG, logcat_tag)
    #         self.command_processor.call(flush_call_array)
    #
    #     call_array = list(_adb_logcat_with_tag)
    #     replace(call_array, _ID_ADB_DEVICE_IDENTIFIER, self.adb_device_identifier)
    #     replace(call_array, _ID_TAG, logcat_tag)
    #     return self.command_processor.Popen(args=call_array, bufsize=4096, stdout=subprocess.PIPE)


    """
    Kills and than starts back up the adb server. Because it. can. be. finnicky.
    """

    def restart_adb_server(self):
        self.command_processor.call(list(_adb_kill_server))
        self.command_processor.call(list(_adb_start_server))

    """
    Used for pausing activity while an emulator is starting up.
    Also includes an indication of the progress
    """

    def wait_for_device_ready(self):
        waitduration = 180
        timepassed = 0
        checkfrequency = 5

        logging.info('Waiting for emulator ' + self.adb_device_identifier + ' to finish starting')
        sys.stdout.flush()

        client_adb_identifier = self.adb_device_identifier

        while timepassed <= waitduration and client_adb_identifier is not None:
            logging.info('.')

            if self.is_fully_booted():
                # if configurationmanager.Configuration.deployment_environments['android_emulator'].sdk_level == 21:
                # unlock_device_nopassword(client_adb_identifier, command_processor)
                client_adb_identifier = None

            time.sleep(checkfrequency)
            timepassed = timepassed + checkfrequency

        logging.info('Emulator ' + self.adb_device_identifier + ' has finished starting.')
