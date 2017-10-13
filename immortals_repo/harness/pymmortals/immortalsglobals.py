import inspect
import logging
import os
import signal
import sys
import traceback
from threading import RLock
from typing import Optional

from .datatypes.routing import EventTags

ANDROID_HOME = os.getenv('ANDROID_HOME')
EMULATOR_BIN = os.path.join(ANDROID_HOME, 'emulator/emulator')
ADB_BIN = os.path.join(ANDROID_HOME, 'platform-tools/adb')
MKSDCARD_BIN = os.path.join(ANDROID_HOME, 'emulator/mksdcard')
AVD_MANAGER_BIN = os.path.join(ANDROID_HOME, 'tools/bin/avdmanager')

DEPLOYMENT_DIRECTORY = None
RESULTS_DIRECTORY = None

PACKAGE_ROOT = os.path.abspath(os.path.dirname(inspect.stack()[0][1]))

_exit_handlers = list()

signal_handlers = []

_olympus = None
_router = None

_process_lock = RLock()


def add_exit_handler(new_handler):
    _exit_handlers.insert(len(_exit_handlers) - 1, new_handler)


def get_event_router():
    """
    :rtype: pymmortals.routing.eventrouter.EventRouter
    """
    global _router
    if _router is None:
        from .routing.eventrouter import EventRouter
        _router = EventRouter()
    return _router


def get_olympus():
    """
    :rtype: Olympus
    """
    global _olympus
    with _process_lock:
        if _olympus is None:
            event_router = get_event_router()
            from . import threadprocessrouter
            from .olympus import Olympus
            _olympus = Olympus(event_router)
            threadprocessrouter.start_thread(thread_method=_olympus.start)

    return _olympus


def start_olympus():
    from . import threadprocessrouter
    o = get_olympus()
    threadprocessrouter.start_thread(thread_method=o.start)


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


def exception_handler(exc_type, exc_value, exc_tb):
    exc_str = ''.join(traceback.format_exception(etype=exc_type, value=exc_value, tb=exc_tb))

    logging.error(exc_str)

    get_event_router().submit(EventTags.THErrorGeneral, '\n'.join(exc_str))

    _exit_handler()


def main_thread_cleanup_hookup():
    signal.signal(signal.SIGINT, _signal_handler)
    # if not get_configuration().throwExceptions:
    sys.excepthook = exception_handler


def force_exit():
    _exit_handler()
