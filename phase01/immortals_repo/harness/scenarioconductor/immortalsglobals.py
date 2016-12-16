import inspect
import logging
import os
import signal
import sys
import traceback

from .data.root_configuration import Configuration
from .packages import commentjson as json
from .reporting.reportinginterface import ReportingInterface

"""
:type __logger: ReportingInterface
"""
IMMORTALS_ROOT = os.path.abspath(os.path.join(os.path.dirname(inspect.stack()[0][1]), '../../')) + '/'

ANDROID_BIN = 'android'
EMULATOR_BIN = 'emulator'
ADB_BIN = 'adb'
MKSDCARD_BIN = 'mksdcard'

DEPLOYMENT_DIRECTORY = None
RESULTS_DIRECTORY = None

PACKAGE_ROOT = os.path.abspath(os.path.dirname(inspect.stack()[0][1]))

exit_handlers = []

failure_handlers = []

signal_handlers = []

_logger = None
_debugMode = False


# config = Configuration

def set_logger(new_logger):
    global _logger
    _logger = new_logger


def logger():
    """
    :rtype: ReportingInterface
    """
    global _logger
    return _logger


def set_debug(value=True):
    global _debugMode
    _debugMode = value


def get_debug():
    """
    :rtype: bool
    """
    return _debugMode


# noinspection PyBroadException
def _exit_handler():
    for handler in exit_handlers:
        try:
            handler()
        except:
            traceback.print_exc()


def _signal_handler(sig, action):
    _exit_handler()

    for handler in signal_handlers:
        handler(sig, action)


def _exception_handler(exc_type, exc_value, exc_tb):
    traceback.print_exception(etype=exc_type, value=exc_value, tb=exc_tb)
    exc_str = traceback.format_exception(etype=exc_type, value=exc_value, tb=exc_tb)

    for h in failure_handlers:
        h(exc_str)

    logging.error(exc_str)
    _exit_handler()


def main_thread_cleanup_hookup():
    signal.signal(signal.SIGINT, _signal_handler)
    sys.excepthook = _exception_handler


def force_exit():
    _exit_handler()


configuration = Configuration.from_dict(
    json.load(open(os.path.join(PACKAGE_ROOT, 'infrastructure_configuration.json'))),
    value_pool={'immortalsRoot': IMMORTALS_ROOT}
)
