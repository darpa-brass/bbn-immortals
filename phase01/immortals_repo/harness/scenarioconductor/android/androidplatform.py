"""
Core android platform. Currently contains emulator usage that can be disabled
by overriding the relevent methods. Assumes ADB usage for deployment
"""

import atexit
import logging

_adb_has_been_reinitialized = False;
_adb_identifier_count = 0
_emulator_name_template = 'emulator-{CONSOLEPORT}'
_port_base = 5560


def _exit_handler():
    logging.info('Cleaning up after androidplatform.py')
    for platform in _instances:
        platform.platform_teardown()


atexit.register(_exit_handler)
_instances = {}


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


class AndroidPlatform(object):
    def platform_setup(self):
        pass

    def deploy_application(self, application_location):
        pass

    def upload_file(self, source_file_location, file_target):
        pass

    def start_application(self):
        pass

    def stop_application(self):
        pass

    def platform_teardown(self):
        pass
