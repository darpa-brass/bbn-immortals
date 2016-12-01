"""
This class contains functions for interacting with ADB
"""

import logging
import sys
import time

from .. import threadprocessrouter as tpr
from ..data.applicationconfig import AndroidApplicationConfig
from ..utils import replace

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
_adb_uninstall_package = (ADB_BIN, '-s', _ID_ADB_DEVICE_IDENTIFIER, 'uninstall', _ID_PACKAGE)
_adb_remove_file_recursively = (ADB_BIN, '-s', _ID_ADB_DEVICE_IDENTIFIER, 'shell', 'rm', '-r', _ID_TARGET_FILEPATH)


def upload_file(adb_device_identifier, source_file_location, file_target, command_processor=tpr):
    """
    Uploads a file to the device specified by adb_device_identifier.
    source_file_location specifies the location of the source file, while
    file_target specifies the new name of the file that will be placed in '/sdcard/'
    """

    args = list(_adb_upload_command)
    replace(args, _ID_ADB_DEVICE_IDENTIFIER, adb_device_identifier)
    replace(args, _ID_SOURCE_FILEPATH, source_file_location)
    replace(args, _ID_TARGET_FILEPATH, file_target)
    command_processor.call(args)


def grant_permission(adb_device_identifier, package, permission_string, command_processor=tpr):
    """
    Grants provided permission_strings to the provided package on the device
    specified by adb_device_identifier
    """

    args = list(_adb_grant_permission_command)
    replace(args, _ID_ADB_DEVICE_IDENTIFIER, adb_device_identifier)
    replace(args, _ID_PACKAGE, package)
    replace(args, _ID_PERMISSION, permission_string)
    command_processor.call(args)


def start_process(adb_device_identifier, package_activity_identifier, command_processor=tpr):
    """
    Starts package_activity_identifier on adb_device_identifier
    """

    args = list(_adb_process_start_command)
    replace(args, _ID_ADB_DEVICE_IDENTIFIER, adb_device_identifier)
    replace(args, _ID_PACKAGE_ACTIVITY, package_activity_identifier)
    command_processor.call(args)


def force_stop_process(adb_device_identifier, package_identifier, command_processor=tpr):
    """
    Performs a forceful stop of all activities and services provided by
    package_identifier on adb_device_identifier
    """

    args = list(_adb_process_stop_command)
    replace(args, _ID_ADB_DEVICE_IDENTIFIER, adb_device_identifier)
    replace(args, _ID_PACKAGE, package_identifier)
    command_processor.call(args)


def deploy_apk(adb_device_identifier, apk_location, command_processor=tpr):
    """
    Deploys the apk specified by apk_location on adb_device_identifier, removing
    the current version on the device if it exists
    """

    args = list(_adb_deploy_apk_command)
    replace(args, _ID_ADB_DEVICE_IDENTIFIER, adb_device_identifier)
    replace(args, _ID_SOURCE_FILEPATH, apk_location)
    command_processor.call(args)


def is_known(adb_device_identifier, command_processor=tpr):
    """
    Indicates if the device provider specified by adb_device_identifier is
    known by adb. Note that just because it is known doesn't mean it is connected,
    online, or finished booting
    """

    args = list(_adb_list_devices)
    list_devices_result = command_processor.check_output(args)
    search_string = adb_device_identifier + '\tdevice'
    return list_devices_result.find(search_string) >= 0


def unlock_device_nopassword(adb_device_identifier, command_processor=tpr):
    """
    Unlocks a device provided it does not have a password
    """

    args = list(_adb_unlock_device_nopassword)
    replace(args, _ID_ADB_DEVICE_IDENTIFIER, adb_device_identifier)
    command_processor.call(args)


def is_fully_booted(adb_device_identifier, command_processor=tpr):
    """
    Indicates if a device has finished booting judging by the state of the boot
    screen.  If the boot screen is disabled, this will not work!
    """

    if is_known(adb_device_identifier, command_processor):
        args = list(_adb_get_device_status_command)
        replace(args, _ID_ADB_DEVICE_IDENTIFIER, adb_device_identifier)
        result = command_processor.check_output(args).strip()
        return result == 'stopped'

    return False


def restart_adb_server(command_processor=tpr):
    """
    Kills and than starts back up the adb server. Because it. can. be. finnicky.
    """

    command_processor.call(list(_adb_kill_server))
    time.sleep(1)
    command_processor.call(list(_adb_start_server))
    time.sleep(2)


def wait_for_device_ready(adb_device_identifier, command_processor=tpr):
    """
    Used for pausing activity while an emulator is starting up.
    Also includes an indication of the progress
    """
    waitduration = 180
    timepassed = 0
    checkfrequency = 5

    logging.info('Waiting for emulator ' + adb_device_identifier + ' to finish starting')
    sys.stdout.flush()

    client_adb_identifier = adb_device_identifier

    while timepassed <= waitduration and client_adb_identifier is not None:
        logging.info('.')

        if is_fully_booted(adb_device_identifier, command_processor):
            client_adb_identifier = None

        time.sleep(checkfrequency)
        timepassed += checkfrequency

    logging.info('Emulator ' + adb_device_identifier + ' has finished starting.')


def uninstall_package(adb_device_identifier, package_identifier, command_processor=tpr):
    args = list(_adb_uninstall_package)
    replace(args, _ID_ADB_DEVICE_IDENTIFIER, adb_device_identifier)
    replace(args, _ID_PACKAGE, package_identifier)
    command_processor.call(args)


def remove_file_recursively(adb_device_identifier, filepath, command_processor=tpr):
    args = list(_adb_remove_file_recursively)
    replace(args, _ID_ADB_DEVICE_IDENTIFIER, adb_device_identifier)
    replace(args, _ID_TARGET_FILEPATH, filepath)
    command_processor.call(args)


class AdbHelper:
    """
    :type config: AndroidApplicationConfig
    """

    def __init__(self, config, adb_device_identifier, command_processor=tpr):
        self.config = config
        self.adb_device_identifier = adb_device_identifier
        self.cp = command_processor

    def upload_file(self, source_file_location, file_target):
        print 'ADB-UF'
        upload_file(self.adb_device_identifier, source_file_location, file_target, self.cp)

    def grant_permission(self, package, permission_string):
        print 'ADB-GP'
        grant_permission(self.adb_device_identifier, package, permission_string, self.cp)

    def start_process(self):
        print 'ADB-SP'
        start_process(self.adb_device_identifier, self.config.package_identifier + '/' + self.config.main_activity, self.cp)

    def force_stop_process(self):
        print 'ADB-FSP'
        force_stop_process(self.adb_device_identifier, self.config.package_identifier, self.cp)

    def deploy_apk(self, apk_location):
        print 'ADB-DA'
        deploy_apk(self.adb_device_identifier, apk_location, self.cp)

    def is_known(self):
        print 'ADB-IK'
        return is_known(self.adb_device_identifier, self.cp)

    def unlock_device_nopassword(self):
        print 'ADB-UDN'
        unlock_device_nopassword(self.adb_device_identifier, self.cp)

    def is_fully_booted(self):
        print 'ADB-IFB'
        return is_fully_booted(self.adb_device_identifier, self.cp)

    def restart_adb_server(self):
        print 'ADB-RAS'
        restart_adb_server(self.cp)

    def wait_for_device_ready(self):
        print 'ADB-WFDR'
        wait_for_device_ready(self.adb_device_identifier, self.cp)

    def uninstall_package(self):
        print 'ADB-UP'
        uninstall_package(self.adb_device_identifier, self.config.package_identifier)

    def remove_file_recursively(self, filepath):
        print 'ADB-RFR'
        remove_file_recursively(self.adb_device_identifier, filepath, self.cp)
