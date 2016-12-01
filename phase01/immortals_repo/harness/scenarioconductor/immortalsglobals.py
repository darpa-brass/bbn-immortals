import inspect
import os
import signal
import sys
import traceback

IMMORTALS_ROOT = os.path.abspath(os.path.join(os.path.dirname(inspect.stack()[0][1]), '../../')) + '/'

exit_handlers = []

def _exit_handler():
    for handler in exit_handlers:
        print "_exit_handler"
        try:
            handler()
        except:
            pass

# noinspection PyUnusedLocal,PyShadowingNames
def _signal_handler(signal, frame):
    print '_signal_handler'
    _exit_handler()


def _exception_handler(exc_type, exc_value, exc_tb):
    print 'Unhandled error: ', exc_type, exc_value, traceback.print_tb(exc_tb)
    _exit_handler()


def main_thread_cleanup_hookup():
    signal.signal(signal.SIGINT, _signal_handler)
    sys.excepthook = _exception_handler


def force_exit():
    _exit_handler()
