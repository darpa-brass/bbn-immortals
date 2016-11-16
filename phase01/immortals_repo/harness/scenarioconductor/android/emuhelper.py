"""
This class is used to perform actions related to the emulator.
"""

import atexit
import logging
import subprocess
from threading import Lock

import configurationmanager
from configurationmanager import ADB_BIN, ANDROID_BIN, EMULATOR_BIN
from utils import replace

_ID_EMULATOR_IDENTIFIER = '$EMULATOR_IDENTIFIER!'
_ID_PORT = '$PORT!'
_ID_EMULATOR_SDK_LEVEL = '$EMULATOR_SDK_LEVEL!'

_android_emulator_create_command = (
ANDROID_BIN, 'create', 'avd', '--target', 'android-' + _ID_EMULATOR_SDK_LEVEL, '--abi', 'default/x86_64', '--device',
'Nexus 5', '--sdcard', '512M', '--name', _ID_EMULATOR_IDENTIFIER)
_android_emulator_delete_command = (ANDROID_BIN, 'delete', 'avd', '--name', _ID_EMULATOR_IDENTIFIER)
# DO NOT disable the boot screen as it is used to determine when a device has finished booting!
_android_emulator_start_command = (EMULATOR_BIN, '-avd', _ID_EMULATOR_IDENTIFIER, '-gpu', 'off', '-port', _ID_PORT)
# _android_emulator_list_command = (EMULATOR_BIN, '-list-avds')
_android_emulator_list_command = (ANDROID_BIN, 'list', 'avd')
_adb_emulator_kill_command = (ADB_BIN, '-s', _ID_EMULATOR_IDENTIFIER, 'emu', 'kill')

_created_processes = []


def _exit_handler():
    for process in _created_processes:
        if not process is None:
            process.kill()


atexit.register(_exit_handler)


class EmuHelper:
    def __init__(self, adb_device_identifier, command_processor=None):
        self.adb_device_identifier = adb_device_identifier

        # Listening to signal exits, I was getting colliding emulator deletion commands, so this prevents tyat.
        self.lock = Lock()

        self.emulator_process = None

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
    Creates an emulator for the formatted emulator_identifier
    """

    def create_emulator(self):
        call_list = list(_android_emulator_create_command)
        replace(call_list, _ID_EMULATOR_IDENTIFIER, self.adb_device_identifier)
        replace(call_list, _ID_EMULATOR_SDK_LEVEL,
                str(configurationmanager.Configuration.deployment_environments['android_emulator'].sdk_level))
        self.command_processor.call(call_list)

    """
    Indicates if an emulator with the provided emulator_identifier exists
    """

    def emulator_exists(self):
        call_list = list(_android_emulator_list_command)
        values = self.command_processor.check_output(call_list)
        return self.adb_device_identifier in values

    """
    Deletes (and attempts to kill) the emulator specified by emulator_identifier if
    it exists
    """

    def delete_emulator(self):
        with self.lock:
            if self.emulator_exists():
                call_list = list(_adb_emulator_kill_command)
                replace(call_list, _ID_EMULATOR_IDENTIFIER, self.adb_device_identifier)
                self.command_processor.call(call_list)

                if self.emulator_process is not None:
                    self.emulator_process.kill()

                call_list = list(_android_emulator_delete_command)
                replace(call_list, _ID_EMULATOR_IDENTIFIER, self.adb_device_identifier)
                # TODO: This should probably go somewhere else...
                self.command_processor.call(call_list)

    """
    Starts an emulator with the name provided by emulator_identifier, returning
    before it is ready to be used.
    """

    def start_emulator(self, console_port, display_ui, sdcard_filepath):
        call_list = list(_android_emulator_start_command)

        if display_ui:
            call_list.append('-skin')
            call_list.append('720x1280')
        else:
            call_list.append('-no-window')

        if sdcard_filepath is not None:
            call_list.append('-sdcard')
            call_list.append(sdcard_filepath)

        replace(call_list, _ID_EMULATOR_IDENTIFIER, self.adb_device_identifier)
        replace(call_list, _ID_PORT, str(console_port))
        self.emulator_process = self.command_processor.Popen(call_list)
        return self.emulator_process

    """
    Attempts to kill the emulator specified by emulator_identifier.  This is not
    guaranteed to work since it requires ADB to know of the device which may not be
    the case if the emulator was just started or ADB is having issues
    """

    def kill_emulator(self):
        with self.lock:
            call_list = list(_adb_emulator_kill_command)
            replace(call_list, _ID_EMULATOR_IDENTIFIER, self.adb_device_identifier)
            self.command_processor.call(call_list)
