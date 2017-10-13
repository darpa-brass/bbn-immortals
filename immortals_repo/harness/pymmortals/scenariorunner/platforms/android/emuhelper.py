"""
This class is used to perform actions related to the emulator.
"""
from threading import Semaphore

import os

from pymmortals.threadprocessrouter import ImmortalsSubprocess, PIPE, global_subprocess, sleep
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.immortalsglobals import ADB_BIN, AVD_MANAGER_BIN, EMULATOR_BIN, get_event_router
from pymmortals.utils import replace, get_formatted_string_value
from . import adbhelper

_ID_EMULATOR_IDENTIFIER = '$EMULATOR_IDENTIFIER!'
_ID_CONSOLE_PORT = '$CONSOLE_PORT!'
# _ID_ADB_PORT = '$ADB_PORT!'
_ID_EMULATOR_SDK_LEVEL = '$EMULATOR_SDK_LEVEL!'

_android_emulator_create_command = (
    AVD_MANAGER_BIN, 'create', 'avd', '--device', '8', '--sdcard', '512M', '--name', _ID_EMULATOR_IDENTIFIER,
    '--package', 'system-images;android-' + _ID_EMULATOR_SDK_LEVEL + ';default;x86_64')

_android_emulator_delete_command = (AVD_MANAGER_BIN, 'delete', 'avd', '--name', _ID_EMULATOR_IDENTIFIER)
# DO NOT disable the boot screen as it is used to determine when a device has finished booting!
_android_emulator_start_command = (
    EMULATOR_BIN, '-avd', _ID_EMULATOR_IDENTIFIER, '-gpu', 'swiftshader', '-port', _ID_CONSOLE_PORT)
_android_emulator_list_command = (AVD_MANAGER_BIN, 'list', 'avd')
_expect_emulator_kill_command = (
    'bash', '-c',
    (
        r'TOKEN=`cat ~/.emulator_console_auth_token`;'
        r'expect -c "spawn telnet 127.0.0.1 ' + _ID_CONSOLE_PORT + r';'
                                                                   r'expect \"OK\";'
                                                                   r'send \"auth ${TOKEN}\r\";'
                                                                   r'expect \"OK\";'
                                                                   r'send \"kill\r\";'
                                                                   r'expect \"OK\";'
                                                                   r'send \"exit\r\""'))
_adb_list_devices = (ADB_BIN, 'devices')

# Used to block multiple emulators from going through their early startup process simultaneously to prevent problems
_emulator_semaphore = Semaphore()  # type: Semaphore

_config = get_configuration()

emulator_name_template = 'emulator-{CONSOLEPORT}'
_port_max = 5584
_port_min = 5554
_port_skip = 2
_port_generator = [n for n in range(_port_min, _port_max + _port_skip, _port_skip)]


def generate_emulator_identifier():
    """
    :rtype: str
    """
    global _port_generator

    try:
        port = _port_generator.pop()
        return emulator_name_template.format(CONSOLEPORT=port)

    except IndexError:
        raise Exception("No more than 16 devices are supported at this time!")


def reset_identifier_counter():
    global _port_generator, _port_max, _port_min, _port_skip
    _port_generator = [p for p in range(_port_min, _port_max + _port_skip, _port_skip)]


def wipe_emulators():
    generator = [e for e in range(_port_min, _port_max + _port_skip, _port_skip)]

    for i in range(0, _config.validationEnvironment.initialEmulatorCount):
        emulator_identifier = emulator_name_template.format(CONSOLEPORT=generator.pop())

        while emulator_exists(emulator_identifier):
            if adbhelper.is_known(emulator_identifier):
                console_port = int(
                    get_formatted_string_value(emulator_name_template, emulator_identifier, 'CONSOLEPORT'))
                kill_emulator(console_port=console_port)

            delete_emulator(emulator_identifier)

            emulator_identifier = emulator_name_template.format(CONSOLEPORT=generator.pop())


def get_sdcard_path(adb_device_identifier: str) -> str:
    return os.path.join(_config.runtimeRoot, adb_device_identifier + '_sdcard.img')


def initially_setup_emulators():
    generator = [e for e in range(_port_max - _config.validationEnvironment.initialEmulatorCount * 2 + 2,
                                  _port_max + _port_skip, _port_skip)]

    for console_port in generator:
        emulator_identifier = emulator_name_template.format(CONSOLEPORT=console_port)

        create_emulator(emulator_identifier)

        start_emulator(
            adb_device_identifier=emulator_identifier,
            console_port=console_port)

        kill_emulator(console_port)


def create_emulator(adb_device_identifier: str, command_processor: ImmortalsSubprocess = global_subprocess):
    cmd = ['mksdcard', '12M', get_sdcard_path(adb_device_identifier=adb_device_identifier)]
    command_processor.run(cmd)

    args = list(_android_emulator_create_command)
    replace(args, _ID_EMULATOR_IDENTIFIER, adb_device_identifier)

    env = None

    for de in _config.deploymentEnvironments:
        if de.identifier == 'android_emulator':
            env = de

    replace(args, _ID_EMULATOR_SDK_LEVEL,
            str(env.sdkLevel))
    command_processor.run(args=args)


def emulator_exists(adb_device_identifier: str, command_processor: ImmortalsSubprocess = global_subprocess) -> bool:
    # You wouldn't think this is needed here, but it sometimes crashes with an NPE otherwise
    with _emulator_semaphore:
        args = list(_android_emulator_list_command)
        values = command_processor.run(args=args, stdout=PIPE).stdout.decode()
        return adb_device_identifier in values


def delete_emulator(adb_device_identifier: str, command_processor: ImmortalsSubprocess = global_subprocess):
    """
    Deletes (and attempts to kill) the emulator specified by emulator_identifier if
    it exists
    """

    if emulator_exists(adb_device_identifier, command_processor):
        with _emulator_semaphore:
            args = list(_android_emulator_delete_command)
            replace(args, _ID_EMULATOR_IDENTIFIER, adb_device_identifier)
            # TODO: This should probably go somewhere else...
            command_processor.run(args=args)


def start_emulator(adb_device_identifier: str, console_port: int,
                   command_processor: ImmortalsSubprocess = global_subprocess, instance_identifier: str = "UNDEFINED"):
    """
    Starts an emulator with the name provided by emulator_identifier, returning
    before it is ready to be used.
    """
    with _emulator_semaphore:
        args = list(_android_emulator_start_command)

        if _config.validationEnvironment.displayAndroidEmulatorGui:
            args.append('-skin')
            args.append('720x1280')
        else:
            args.append('-no-window')

        args.append('-sdcard')
        args.append(get_sdcard_path(adb_device_identifier))

        replace(args, _ID_EMULATOR_IDENTIFIER, adb_device_identifier)
        replace(args, _ID_CONSOLE_PORT, str(console_port))

        if get_configuration().validationEnvironment.androidEmulatorQemuArgs is not None \
                and len(get_configuration().validationEnvironment.androidEmulatorQemuArgs) > 0:
            args.append('-qemu')

            for arg in get_configuration().validationEnvironment.androidEmulatorQemuArgs:
                args.append(arg)

        if _config.debugMode:
            get_event_router().log_time_delta_0(instance_identifier, 'startEmulatorPhase1')
            command_processor.Popen(args=args)
            while not adbhelper.is_known(adb_device_identifier, command_processor):
                sleep(1)
            get_event_router().log_time_delta_1(instance_identifier, 'startEmulatorPhase1')

        else:
            command_processor.Popen(args=args)
            while not adbhelper.is_known(adb_device_identifier, command_processor):
                sleep(1)

    if _config.debugMode:
        get_event_router().log_time_delta_0(instance_identifier, 'startEmulatorPhase2')
        adbhelper.wait_for_device_ready(adb_device_identifier, command_processor)
        get_event_router().log_time_delta_1(instance_identifier, 'startEmulatorPhase2')
    else:
        adbhelper.wait_for_device_ready(adb_device_identifier, command_processor)


def kill_emulator(console_port: int, command_processor: ImmortalsSubprocess = global_subprocess):
    """
    Attempts to kill the emulator specified by emulator_identifier.  This is not
    guaranteed to work since it requires ADB to know of the device which may not be
    the case if the emulator was just started or ADB is having issues
    """

    with _emulator_semaphore:
        args = list(_expect_emulator_kill_command)
        replace(args, _ID_CONSOLE_PORT, str(console_port))
        command_processor.run(args=args)


class EmuHelper:
    def __init__(self, adb_device_identifier: str, console_port: int,
                 command_processor: ImmortalsSubprocess,
                 instance_identifier: str = None):
        self.adb_device_identifier: str = adb_device_identifier
        self.console_port: int = console_port
        self.sdcard_filepath: str = os.path.join(os.getenv('HOME'), adb_device_identifier + '_sdcard.img')
        self.cp: ImmortalsSubprocess = command_processor
        self.instance_identifier: str = instance_identifier

    def create_emulator(self):
        create_emulator(self.adb_device_identifier, self.cp)

    def emulator_exists(self) -> bool:
        return emulator_exists(self.adb_device_identifier, self.cp)

    def delete_emulator(self):
        delete_emulator(self.adb_device_identifier, self.cp)

    def start_emulator(self):
        start_emulator(self.adb_device_identifier, self.console_port, self.cp, self.instance_identifier)

    def kill_emulator(self):
        kill_emulator(self.console_port, self.cp)
