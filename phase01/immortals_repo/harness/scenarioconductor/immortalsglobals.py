import inspect
import logging
import os
import signal
import sys
import traceback

from data.base.root_configuration import Configuration
from .olympus import Olympus
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

_olympus = None


def get_olympus():
    """
    :rtype: Olympus
    """
    global _olympus
    if _olympus is None:
        _olympus = Olympus(host=configuration.testAdapter.url, port=configuration.testAdapter.port)
    return _olympus


def start_olympus():
    from . import threadprocessrouter as tpr

    o = get_olympus()

    tpr.start_thread(thread_method=o.start)


def set_logger(new_logger):
    global _logger
    _logger = new_logger


def logger():
    """
    :rtype: ReportingInterface
    """
    global _logger
    return _logger


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


def _load_configuration():
    configuration_d = json.load(open(os.path.join(PACKAGE_ROOT, 'root_configuration.json')))

    override_filepath = os.path.join(configuration_d['dataRoot'], 'environment.json')
    if os.path.exists(override_filepath):
        override_configuration_d = json.load(open(override_filepath))

        for key in override_configuration_d:
            target_d = configuration_d
            override_path = key.split('.')
            override_tail = override_path.pop()

            for path_element in override_path:
                target_d = target_d[path_element]

            target_d[override_tail] = override_configuration_d[key]

    return Configuration.from_dict(
        configuration_d,
        value_pool={'immortalsRoot': IMMORTALS_ROOT}
    )


configuration = _load_configuration()  # type: Configuration
