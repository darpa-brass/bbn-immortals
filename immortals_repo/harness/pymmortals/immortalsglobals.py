import collections
import inspect
import json
import logging
import os
import signal
import sys
import traceback
from threading import RLock

from pymmortals.generated.mil.darpa.immortals.config.immortalsconfig import ImmortalsConfig
from pymmortals.resources import resourcemanager
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

_olympus = None
_router = None
_shutting_down = False  # type: bool

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
    global _exit_code, _process_lock, _shutting_down

    with _process_lock:
        if not _shutting_down:
            _shutting_down = True
            for handler in _exit_handlers:
                try:
                    handler()
                except:
                    traceback.print_exc()

            sys.exit(_exit_code)


def _signal_handler(sig, action):
    _exit_handler()


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


_configuration = None  # type: ImmortalsConfig
_load_lock = RLock()
_exit_code = 0  # type: int


def set_exit_code(code: int):
    global _exit_code
    _exit_code = code


def get_exit_code() -> int:
    global _exit_code
    return _exit_code


def _load_configuration():
    """
    :rtype: ImmortalsConfig
    """
    with _load_lock:
        from pymmortals.generated.mil.darpa.immortals.config.immortalsconfig import ImmortalsConfig
        configuration_d = resourcemanager.load_immortals_configuration()
        override_configuration_d = None

        override_filepath = os.getenv('IMMORTALS_OVERRIDE_FILE')
        if override_filepath is not None and os.path.exists(override_filepath):
            override_lines_string = ''
            with open(override_filepath) as f:
                for line in iter(f):  # type: str
                    if not line.lstrip(' ').startswith('#') and not line.lstrip(' ').startswith('//'):
                        override_lines_string += line

            override_configuration_d = json.loads(override_lines_string)

        if override_configuration_d is not None:
            def update(original, overrides):
                for k, v in overrides.items():
                    if isinstance(v, collections.Mapping):
                        original[k] = update(original.get(k, {}), v)
                    else:
                        original[k] = v
                return original

            configuration_d = update(configuration_d, override_configuration_d)

    return ImmortalsConfig.from_dict(d=configuration_d)


def get_configuration():
    """
    :rtype: ImmortalsConfig
    """
    global _configuration

    if _configuration is None:
        _configuration = _load_configuration()

        roots = [
            _configuration.globals.globalWorkingDirectory,
            _configuration.globals.globalLogDirectory,
            _configuration.globals.globalApplicationDeploymentDirectory,
            _configuration.globals.executionsDirectory,
            _configuration.globals.executionsLogDirectory
        ]
        for r in roots:
            if not os.path.exists(r):
                os.mkdir(r)

    return _configuration


if __name__ == '__main__':
    cfg = get_configuration()
