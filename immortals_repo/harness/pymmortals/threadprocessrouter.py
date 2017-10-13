import logging
import os
import subprocess
import sys
import time
import traceback
from collections import OrderedDict
from io import BufferedIOBase, BufferedReader
from subprocess import TimeoutExpired, CalledProcessError, CompletedProcess
from threading import Thread, Timer, RLock

from pymmortals import immortalsglobals as ig
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.routing import EventTags

_shutting_down = False
_lock = RLock()

_thread_process_stack = []

_cleanup_polling_interval_seconds = 10

PIPE = subprocess.PIPE


class _StdEndpointSet:
    def __init__(self, stdout_absfilepath: str, stderr_absfilepath: str,
                 log_out_to_host: bool = False, log_err_to_host: bool = False):
        self.out_filepath = stdout_absfilepath
        self.out: BufferedIOBase = open(stdout_absfilepath, 'w')

        self.err_filepath = stderr_absfilepath
        self.err: BufferedIOBase = open(stderr_absfilepath, 'w')

        self.out_in: BufferedReader = open(stdout_absfilepath, 'r') if log_out_to_host else None
        self.err_in: BufferedReader = open(stderr_absfilepath, 'r') if log_err_to_host else None

    def write_out_line(self, message):
        self.out.write(message + '\n')
        self.out.flush()


_std_endpoints = OrderedDict()


def __flush_to_cli():
    for ep in _std_endpoints:  # type: _StdEndpointSet
        if ep.err_in is not None:
            ep.err.flush()
            val = ep.err_in.readline()
            while val != '':
                print(val, file=sys.stderr)

        if ep.out_in is not None:
            ep.out.flush()
            val = ep.out_in.readline()
            while val != '':
                print(val, file=sys.stderr)


def get_std_endpoint(log_dirpath: str, file_tag: str) -> _StdEndpointSet:
    if file_tag not in _std_endpoints:
        tag = file_tag + '_'
        out = os.path.join(log_dirpath, tag + 'stdout.txt')
        err = os.path.join(log_dirpath, tag + 'stderr.txt')

        _std_endpoints[file_tag] = _StdEndpointSet(stdout_absfilepath=out, stderr_absfilepath=err)

    return _std_endpoints[file_tag]


class ImmortalsSubprocess:
    def __init__(self, command_processor: 'ImmortalsSubprocess' or None,
                 log_tag: str, log_dirpath: str = None):
        self.__command_processor: 'ImmortalsSubprocess' or subprocess = \
            command_processor if command_processor is not None else subprocess

        if log_dirpath is None:
            log_dirpath = get_configuration().artifactRoot

        endpoint = get_std_endpoint(log_dirpath=log_dirpath, file_tag=log_tag)
        self.__err = endpoint.err
        self.__out = endpoint.out

    @property
    def _command_processor(self) -> 'ImmortalsSubprocess' or subprocess:
        return self.__command_processor

    def Popen(self, args, bufsize=-1, executable=None, stdin=None, stdout: BufferedIOBase = None,
              stderr: BufferedIOBase = None, preexec_fn=None, close_fds=True, shell=False, cwd=None, env=None,
              universal_newlines=False, startupinfo=None, creationflags=0, restore_signals=True,
              start_new_session=False, pass_fds=()):

        process = None

        with _lock:
            if not _shutting_down:

                if stdout is None and self.__out is not None:
                    stdout = self.__out

                if stderr is None and self.__err is not None:
                    stderr = self.__err

                if self._command_processor == subprocess:
                    s_args = str(args)
                    logging.debug('EXEC: ' + s_args)

                    process = self._command_processor.Popen(
                        args, bufsize=bufsize, executable=executable, stdin=stdin, stdout=stdout, stderr=stderr,
                        preexec_fn=preexec_fn, close_fds=close_fds, shell=shell, cwd=cwd, env=env,
                        universal_newlines=universal_newlines, startupinfo=startupinfo, creationflags=creationflags,
                        restore_signals=restore_signals, start_new_session=start_new_session, pass_fds=pass_fds)

                    obj = _ThreadProcessInstance(process=process,
                                                 identifier=s_args)
                    _thread_process_stack.append(obj)

                elif isinstance(self._command_processor, ImmortalsSubprocess):
                    process = self._command_processor.Popen(
                        args, bufsize=bufsize, executable=executable, stdin=stdin, stdout=stdout, stderr=stderr,
                        preexec_fn=preexec_fn, close_fds=close_fds, shell=shell, cwd=cwd, env=env,
                        universal_newlines=universal_newlines, startupinfo=startupinfo, creationflags=creationflags,
                        restore_signals=restore_signals, start_new_session=start_new_session, pass_fds=pass_fds)

        if _shutting_down:
            sys.exit()

        else:
            return process

    # noinspection PyUnusedLocal
    def run(self, *popenargs, stdout: BufferedIOBase = None, stderr: BufferedIOBase = None, input=None, timeout=None,
            check=False, **kwargs) -> CompletedProcess:

        if input is not None:
            if 'stdin' in kwargs:
                raise ValueError('stdin and input arguments may not both be used.')
            kwargs['stdin'] = PIPE

        with self.Popen(stdout=PIPE, stderr=PIPE, *popenargs, **kwargs) as process:
            try:
                stdout, stderr = process.communicate(input, timeout=timeout)
            except TimeoutExpired:
                process.kill()
                stdout, stderr = process.communicate()
                raise TimeoutExpired(process.args, timeout, output=stdout,
                                     stderr=stderr)
            except:
                process.kill()
                process.wait()
                raise
            retcode = process.poll()
            if check and retcode:
                raise CalledProcessError(retcode, process.args,
                                         output=stdout, stderr=stderr)

            return CompletedProcess(process.args, retcode, stdout, stderr)


# I hate the exception handling in this function... But since it sounds like LL reserves the right for a blunt poweroff
# if an error is reported, It's better to try and get all the data possible, even if it means hacky exception handling
# like this to prevent other threads from closing streams mid-function...
# noinspection PyBroadException,PyPep8
def flush_logging_endpoints():
    for endpoint_identifier in _std_endpoints:  # type: _StdEndpointSet
        endpoint = _std_endpoints[endpoint_identifier]

        if endpoint.out is not None and not endpoint.out.closed:
            try:
                endpoint.out.flush()
            except:
                pass
        if endpoint.err is not None and not endpoint.err.closed:
            try:
                endpoint.err.flush()
            except:
                pass


class _ThreadProcessInstance:
    """
    :type process: subprocess_Popen
    :type thread: Thread
    :type timer: Timer
    :type halt_on_shutdown: bool
    """

    def __init__(self, process=None, thread=None, timer=None, halt_on_shutdown=True, shutdown_method=None,
                 shutdown_args=(), identifier=None):
        self.process = process
        self.thread = thread
        self.timer = timer
        self.halt_on_shutdown = halt_on_shutdown
        self.shutdown_method = shutdown_method
        self.shutdown_args = shutdown_args
        self.identifier = identifier

    def is_running(self):
        if self.process is not None:
            try:
                # Using call instead of poll since poll results in no return value if a thread is terminated
                value = self.process.wait(0)
                return value is None
            except TimeoutExpired:
                return True

        elif self.thread is not None:
            return self.thread.is_alive()

        else:
            return self.timer is not None


# noinspection PyBroadException,PyPep8
def _shutdown_tpi(tpi):
    """
    :type tpi: _ThreadProcessInstance
    """
    if tpi.halt_on_shutdown:

        if tpi.shutdown_method is not None:
            try:
                tpi.shutdown_method(*tpi.shutdown_args)
            except:
                pass

        elif tpi.process is not None:
            tpi.process.terminate()

        elif tpi.timer is not None:
            try:
                tpi.timer.cancel()
                # Since the timer does not have a state value, None is used to indicate it is no longer running
                tpi.timer = None
            except:
                pass
        pass


def exit_handler():
    print('SHUTTING DOWN.... Please wait several seconds to shutdown processes and threads...')
    global _shutting_down

    if not _shutting_down:

        with _lock:
            _shutting_down = True

            for tpi in _thread_process_stack:
                _shutdown_tpi(tpi)

            counter = 0
            continue_loop = True
            while counter < 8 and continue_loop:
                time.sleep(1)
                continue_loop = False
                for tpi in _thread_process_stack:  # type: _ThreadProcessInstance
                    if tpi.is_running():
                        continue_loop = True
                        break

                counter += 1

            for tpi in _thread_process_stack:  # type: _ThreadProcessInstance
                if tpi.is_running():
                    if tpi.thread is None or not tpi.thread.isDaemon():

                        if tpi.process is not None:
                            logging.error('COULD NOT GRACEFULLY TERMINATE PROCESS WITH THE ARGUMENTS ' + str(
                                tpi.process.__dict__.get('args')) + '!  KILLING IT NOW!!!')
                            tpi.process.kill()

                        elif tpi.thread is not None:
                            thread_args = tpi.thread.__dict__.get('_Thread__args')
                            logging.error('COULD NOT GRACEFULLY TERMINATE THREAD WITH THE FOLLOWING INFORMATION:' +
                                          '\n\tTHREAD ARGUMENTS: ' + str(thread_args) +
                                          '\n\tSHUTDOWN HANDLER: ' + str(tpi.shutdown_method) +
                                          '\n\tSHUTDOWN_HANDLER_ARGS: ' + str(tpi.shutdown_args) + '!')

                # Do the endpoints last so all log data is written
                for endpoint_identifier in reversed(_std_endpoints):  # type: _StdEndpointSet
                    endpoint = _std_endpoints[endpoint_identifier]
                    if isinstance(endpoint.out, BufferedIOBase) and not endpoint.out.closed:
                        endpoint.out.flush()
                        endpoint.out.close()

                    if isinstance(endpoint.err, BufferedIOBase) and not endpoint.err.closed:
                        endpoint.err.flush()
                        endpoint.err.close()
    print("FIN")


def cleanup_the_dead():
    with _lock:
        deads = []
        for obj in _thread_process_stack:  # type: _ThreadProcessInstance
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


# noinspection PyBroadException,PyPep8
def _thread_executor(method, swallow_and_shutdown_on_exception, args=()):
    try:
        method(*args)

    except Exception as e:  # type: Exception
        msg = traceback.format_exc()
        # logging.error(msg)

        if swallow_and_shutdown_on_exception:
            ig.exception_handler(*sys.exc_info())

        else:
            logging.error(traceback.format_exc())


def start_runtime_loop():
    global _shutting_down

    while not _shutting_down:
        time.sleep(1)


def sleep(duration):
    counter = 0
    while not _shutting_down and counter < duration:
        time.sleep(1)
        counter += 1


def start_thread(thread_method, thread_args=(), shutdown_method=None, shutdown_args=(),
                 swallow_and_shutdown_on_exception=True):
    """
    :rtype: Thread
    """
    with _lock:
        args = [thread_method, swallow_and_shutdown_on_exception, thread_args]

        thread = Thread(target=_thread_executor, args=args)

        obj = _ThreadProcessInstance(thread=thread, shutdown_method=shutdown_method, shutdown_args=shutdown_args,
                                     identifier=str(thread_method))

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
    obj = _ThreadProcessInstance(halt_on_shutdown=halt_on_shutdown, timer=t, identifier='Timer')
    _thread_process_stack.append(obj)
    t.start()
    return t


global_subprocess = \
    ImmortalsSubprocess(command_processor=subprocess, log_dirpath=get_configuration().artifactRoot, log_tag='global')

ig.add_exit_handler(exit_handler)
