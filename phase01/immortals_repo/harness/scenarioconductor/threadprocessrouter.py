import logging
import sys
import traceback
from subprocess import Popen as subprocess_Popen
from threading import Thread, Semaphore, Timer
import immortalsglobals

from packages import subprocess32 as subprocess

_shutting_down = False
_semaphore = Semaphore()

_thread_process_stack = []

_cleanup_polling_interval_seconds = 10

PIPE = subprocess.PIPE


class ThreadProcessInstance:
    """
    :type process: subprocess_Popen
    :type thread: Thread
    :type timer: Timer
    :type halt_on_shutdown: bool
    """

    def __init__(self, process=None, thread=None, timer=None, halt_on_shutdown=True, shutdown_method=None,
                 shutdown_args=()):
        self.process = process
        self.thread = thread
        self.timer = timer
        self.halt_on_shutdown = halt_on_shutdown
        self.shutdown_method = shutdown_method
        self.shutdown_args = shutdown_args


def exit_handler():
    global _shutting_down

    if not _shutting_down:

        with _semaphore:

            while len(_thread_process_stack) > 0:
                obj = _thread_process_stack.pop()  # type: ThreadProcessInstance

                if obj.halt_on_shutdown:

                    if obj.shutdown_method is not None:
                        try:
                            obj.shutdown_method(*obj.shutdown_args)
                        except:
                            pass

                    elif obj.process is not None:
                        try:
                            obj.process.terminate()
                        except:
                            pass

                    elif obj.timer is not None:
                        try:
                            obj.timer.cancel()
                        except:
                            pass

            _shutting_down = True

immortalsglobals.exit_handlers.append(exit_handler)

def cleanup_the_dead():
    with _semaphore:
        deads = []
        for obj in _thread_process_stack:  # type: ThreadProcessInstance
            if obj.process is not None:
                if obj.process.poll() is not None:
                    deads.append(obj)

            elif obj.thread is not None:
                if not obj.thread.isAlive():
                    deads.append(obj)

        for d in deads:
            _thread_process_stack.remove(d)


def keep_running():
    global _shutting_down
    return not _shutting_down


def _thread_executor(method, args=()):
    try:
        method(*args)

    except Exception as e:
        traceback.print_exc(sys.exc_traceback)
        immortalsglobals.force_exit()


def start_thread(thread_method, thread_args=(), shutdown_method=None, shutdown_args=()):
    with _semaphore:
        args = [thread_method, thread_args]

        thread = Thread(target=_thread_executor, args=args)

        obj = ThreadProcessInstance(thread=thread, shutdown_method=shutdown_method, shutdown_args=shutdown_args)

        if shutdown_method is None:
            thread.daemon = True

        _thread_process_stack.append(obj)

        thread.start()

    return thread


def start_timer(duration_seconds, shutdown_method=None, shutdown_args=(), halt_on_shutdown=True):
    t = Timer(
            duration_seconds,
            shutdown_method,
            shutdown_args
    )
    obj = ThreadProcessInstance(halt_on_shutdown=halt_on_shutdown, timer=t)
    _thread_process_stack.append(obj)
    t.start()
    return t


# noinspection PyPep8Naming
def Popen(args, bufsize=0, executable=None, stdin=None, stdout=None, stderr=None, preexec_fn=None,
          close_fds=False, shell=False, cwd=None, env=None, universal_newlines=False, startupinfo=None,
          creationflags=0, command_processor=None, halt_on_shutdown=True, shutdown_method=None, shutdown_args=()):
    process = None

    with _semaphore:
        if not _shutting_down:

            if command_processor is None:
                command_processor = subprocess

            logging.debug('EXEC: ' + str(args))

            process = command_processor.Popen(args, bufsize, executable, stdin, stdout, stderr, preexec_fn, close_fds,
                                              shell, cwd, env, universal_newlines, startupinfo, creationflags)

            obj = ThreadProcessInstance(process=process, halt_on_shutdown=halt_on_shutdown,
                                        shutdown_method=shutdown_method,
                                        shutdown_args=shutdown_args)
            _thread_process_stack.append(obj)

    if _shutting_down:
        sys.exit()

    else:
        return process


def call(args, command_processor=None, halt_on_shutdown=True, shutdown_method=None, shutdown_args=(), *popenargs,
         **kwargs):
    return Popen(args=args, command_processor=command_processor, halt_on_shutdown=halt_on_shutdown,
                 shutdown_method=shutdown_method, shutdown_args=shutdown_args, *popenargs, **kwargs).wait()


def check_output(args, command_processor=None, halt_on_shutdown=True, shutdown_method=None, shutdown_args=(),
                 cwd=None, *popenargs, **kwargs):
    if 'stdout' in kwargs:
        raise ValueError('stdout argument not allowed, it will be overridden.')
    process = Popen(args=args, command_processor=command_processor, halt_on_shutdown=halt_on_shutdown,
                    shutdown_method=shutdown_method, shutdown_args=shutdown_args, stdout=subprocess.PIPE, cwd=cwd,
                    *popenargs,
                    **kwargs)
    output, unused_err = process.communicate()
    retcode = process.poll()
    if retcode:
        cmd = kwargs.get("args")
        if cmd is None:
            cmd = args
        raise subprocess.CalledProcessError(retcode, cmd, output=output)
    return output


def check_call(args, command_processor=None, halt_on_shutdown=True, shutdown_method=None, shutdown_args=(), *popenargs,
               **kwargs):
    retcode = call(args=args, command_processor=command_processor, halt_on_shutdown=halt_on_shutdown,
                   shutdown_method=shutdown_method, shutdown_args=shutdown_args, *popenargs, **kwargs)
    if retcode:
        cmd = kwargs.get("args")
        if cmd is None:
            cmd = popenargs[0]
        raise subprocess.CalledProcessError(retcode, cmd)
    return 0
