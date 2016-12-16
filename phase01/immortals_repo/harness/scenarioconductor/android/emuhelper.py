"""
This class is used to perform actions related to the emulator.
"""

import time
from threading import Semaphore

import adbhelper
from .. import immortalsglobals as ig
from .. import threadprocessrouter as tpr
from ..immortalsglobals import ADB_BIN, ANDROID_BIN, EMULATOR_BIN, configuration
from ..utils import replace

_ID_EMULATOR_IDENTIFIER = '$EMULATOR_IDENTIFIER!'
_ID_CONSOLE_PORT = '$CONSOLE_PORT!'
# _ID_ADB_PORT = '$ADB_PORT!'
_ID_EMULATOR_SDK_LEVEL = '$EMULATOR_SDK_LEVEL!'

_android_emulator_create_command = (
    ANDROID_BIN, 'create', 'avd', '--target', 'android-' + _ID_EMULATOR_SDK_LEVEL, '--abi', 'default/x86_64',
    '--device',
    'Nexus 5', '--sdcard', '512M', '--name', _ID_EMULATOR_IDENTIFIER)
_android_emulator_delete_command = (ANDROID_BIN, 'delete', 'avd', '--name', _ID_EMULATOR_IDENTIFIER)
# DO NOT disable the boot screen as it is used to determine when a device has finished booting!
_android_emulator_start_command = (
    EMULATOR_BIN, '-avd', _ID_EMULATOR_IDENTIFIER, '-gpu', 'off', '-port', _ID_CONSOLE_PORT)
_android_emulator_list_command = (ANDROID_BIN, 'list', 'avd')
_expect_emulator_kill_command = ('bash', '-c',
                                 r'TOKEN=`cat ~/.emulator_console_auth_token`;expect -c "spawn telnet 127.0.0.1 ' + _ID_CONSOLE_PORT + r';expect \"OK\";send \"auth ${TOKEN}\r\";expect \"OK\";send \"kill\r\";expect \"OK\";send \"exit\r\""')
_adb_list_devices = (ADB_BIN, 'devices')

# Used to block multiple emulators from going through their early startup process simultaneously to prevent problems
_emulator_semaphore = Semaphore()  # type: Semaphore


def create_emulator(adb_device_identifier, command_processor=tpr):
    """
    Creates an emulator for the formatted emulator_identifier
    """

    args = list(_android_emulator_create_command)
    replace(args, _ID_EMULATOR_IDENTIFIER, adb_device_identifier)

    env = None

    for de in configuration.deploymentEnvironments:
        if de.identifier == 'android_emulator':
            env = de

    replace(args, _ID_EMULATOR_SDK_LEVEL,
            str(env.sdkLevel))
    command_processor.call(args=args)


def emulator_exists(adb_device_identifier, command_processor=tpr):
    """
    Indicates if an emulator with the provided emulator_identifier exists
    """

    # You wouldn't think this is needed here, but it sometimes crashes with an NPE otherwise
    with _emulator_semaphore:
        args = list(_android_emulator_list_command)
        values = command_processor.check_output(args=args)
        return adb_device_identifier in values


def delete_emulator(adb_device_identifier, command_processor=tpr):
    """
    Deletes (and attempts to kill) the emulator specified by emulator_identifier if
    it exists
    """

    if emulator_exists(adb_device_identifier, command_processor):
        with _emulator_semaphore:
            args = list(_android_emulator_delete_command)
            replace(args, _ID_EMULATOR_IDENTIFIER, adb_device_identifier)
            # TODO: This should probably go somewhere else...
            command_processor.call(args=args)


def start_emulator(adb_device_identifier, console_port, sdcard_filepath, command_processor=tpr):
    """
    Starts an emulator with the name provided by emulator_identifier, returning
    before it is ready to be used.
    """

    with _emulator_semaphore:
        args = list(_android_emulator_start_command)

        if ig.config.displayEmulatorGui:
            args.append('-skin')
            args.append('720x1280')
        else:
            args.append('-no-window')

        if sdcard_filepath is not None:
            args.append('-sdcard')
            args.append(sdcard_filepath)

        replace(args, _ID_EMULATOR_IDENTIFIER, adb_device_identifier)
        replace(args, _ID_CONSOLE_PORT, str(console_port))

        command_processor.Popen(args=args, halt_on_shutdown=ig.config.lifecycle.haltEnvironment)

        while not adbhelper.is_known(adb_device_identifier, command_processor):
            time.sleep(1)

    adbhelper.wait_for_device_ready(adb_device_identifier, command_processor)


def kill_emulator(console_port, command_processor=tpr):
    """
    Attempts to kill the emulator specified by emulator_identifier.  This is not
    guaranteed to work since it requires ADB to know of the device which may not be
    the case if the emulator was just started or ADB is having issues
    """

    with _emulator_semaphore:
        args = list(_expect_emulator_kill_command)
        replace(args, _ID_CONSOLE_PORT, str(console_port))
        command_processor.call(args=args)


class EmuHelper:
    def __init__(self, adb_device_identifier, console_port, sdcard_filepath, command_processor=tpr):
        self.adb_device_identifier = adb_device_identifier
        self.console_port = console_port
        self.sdcard_filepath = sdcard_filepath
        self.cp = command_processor

    def create_emulator(self):
        create_emulator(self.adb_device_identifier, self.cp)

    def emulator_exists(self):
        return emulator_exists(self.adb_device_identifier, self.cp)

    def delete_emulator(self):
        delete_emulator(self.adb_device_identifier, self.cp)

    def start_emulator(self):
        start_emulator(self.adb_device_identifier, self.console_port, self.sdcard_filepath, self.cp)

    def kill_emulator(self):
        kill_emulator(self.console_port, self.cp)
