import logging
import os
import sys
import time
import traceback
from collections import OrderedDict
from subprocess import Popen as subprocess_Popen
from threading import Thread, Semaphore, Timer

from . import immortalsglobals as ig
from .packages import subprocess32 as subprocess
from .packages.subprocess32 import TimeoutExpired

_shutting_down = False
_semaphore = Semaphore()

_thread_process_stack = []
_std_endpoints = OrderedDict()
_logging_endpoints = OrderedDict()

_cleanup_polling_interval_seconds = 10

PIPE = subprocess.PIPE


class LoggingEndpoint:
    sem = Semaphore()

    def __init__(self, output_filepath, das_log_file_error=False):
        try:
            handler = logging.FileHandler(output_filepath)
            handler.setFormatter(logging.Formatter('%(message)s'))
            logging.getLogger()
            self.logger = logging.getLogger(output_filepath)
            self.logger.setLevel(logging.INFO)
            self.logger.addHandler(handler)
            self.das_log_file_error = das_log_file_error
        except Exception as e:
            if das_log_file_error:
                ig.logger().error_das_log_file(message=traceback.format_exc())

            raise e

    def write(self, msg):
        if self.das_log_file_error:
            try:
                self.logger.info(msg=msg)
            except Exception as e:
                print(msg)
                ig.logger().error_das_log_file(message=traceback.format_exc())
                raise e

        else:
            self.logger.info(msg)


class StdEndpointSet:
    """
    :param file err: The error stream
    :param file out: the output stream
    """

    # def __init__(self, file_tag, std_dirpath=None):
    def __init__(self, stdout_absfilepath=None, stderr_absfilepath=None):

        if stdout_absfilepath is None:
            self.out = sys.stdout
        else:
            self.out = open(stdout_absfilepath, 'w')

        if stderr_absfilepath is None:
            self.err = sys.stderr
        else:
            self.err = open(stderr_absfilepath, 'w')

    def write_out_line(self, message):
        self.out.write(message + '\n')
        self.out.flush()


def get_logging_endpoint(log_filepath, das_log_file_error=False):
    with LoggingEndpoint.sem:
        if log_filepath not in _logging_endpoints:
            _logging_endpoints[log_filepath] = LoggingEndpoint(log_filepath, das_log_file_error)

        return _logging_endpoints[log_filepath]


def get_std_endpoint(execution_dirpath, file_tag=None):
    """
    :param str execution_dirpath:
    :param str file_tag:
    :rtype: StdEndpointSet
    """

    if file_tag not in _std_endpoints:
        tag = '' if file_tag is None else file_tag + '_'
        out = os.path.join(execution_dirpath, tag + 'stdout.txt')
        err = os.path.join(execution_dirpath, tag + 'stderr.txt')

        _std_endpoints[file_tag] = StdEndpointSet(stdout_absfilepath=out, stderr_absfilepath=err)

    return _std_endpoints[file_tag]


# I hate the exception handling in this function... But since it sounds like LL reserves the right for a blunt poweroff
# if an error is reported, It's better to try and get all the data possible, even if it means hacky exception handling
# like this to prevent other threads from closing streams mid-function...
# noinspection PyBroadException
def flush_logging_endpoints():
    for endpoint_identifier in _std_endpoints:  # type: StdEndpointSet
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


# noinspection PyBroadException
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
    print 'SHUTTING DOWN.... Please wait several seconds to shutdown processes and threads...'
    global _shutting_down

    if not _shutting_down:

        with _semaphore:
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
                for endpoint_identifier in reversed(_std_endpoints):  # type: StdEndpointSet
                    endpoint = _std_endpoints[endpoint_identifier]
                    if endpoint.out is file and not endpoint.out.closed:
                        endpoint.out.flush()
                        endpoint.out.close()

                    if endpoint.err is file and not endpoint.err.closed:
                        endpoint.err.flush()
                        endpoint.err.close()


def cleanup_the_dead():
    with _semaphore:
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


# noinspection PyBroadException
def _thread_executor(method, args=(), swallow_and_shutdown_on_exception=True):
    try:
        method(*args)

    except Exception as e:  # type: Exception
        # TODO: This could be obsoleted since exceptions are caught within immortalsglobals anyways...
        if swallow_and_shutdown_on_exception:
            # exc_type, exc_value, exc_tb = sys.exc_info()
            msg = traceback.format_exc()
            logging.error(msg)
            for h in ig.failure_handlers:
                h(msg)
            ig.force_exit()
        else:
            raise e


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
    with _semaphore:
        args = [thread_method, thread_args, swallow_and_shutdown_on_exception]

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


# noinspection PyPep8Naming
def Popen(args, bufsize=0, executable=None, stdin=None, stdout=None, stderr=None, preexec_fn=None,
          close_fds=False, shell=False, cwd=None, env=None, universal_newlines=False, startupinfo=None,
          creationflags=0, command_processor=None, halt_on_shutdown=True, shutdown_method=None, shutdown_args=()):
    process = None

    with _semaphore:
        if not _shutting_down:

            if command_processor is None:
                command_processor = subprocess

            s_args = str(args)
            logging.debug('EXEC: ' + s_args)

            process = command_processor.Popen(args, bufsize, executable, stdin, stdout, stderr, preexec_fn, close_fds,
                                              shell, cwd, env, universal_newlines, startupinfo, creationflags)

            obj = _ThreadProcessInstance(process=process, halt_on_shutdown=halt_on_shutdown,
                                         shutdown_method=shutdown_method,
                                         shutdown_args=shutdown_args,
                                         identifier=s_args
                                         )
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


def check_call(args, command_processor=None, halt_on_shutdown=True, shutdown_method=None, shutdown_args=(),
               *popenargs, **kwargs):
    retcode = call(args=args, command_processor=command_processor, halt_on_shutdown=halt_on_shutdown,
                   shutdown_method=shutdown_method, shutdown_args=shutdown_args, *popenargs, **kwargs)
    if retcode:
        cmd = kwargs.get("args")
        if cmd is None:
            cmd = popenargs[0]
        raise subprocess.CalledProcessError(retcode, cmd)
    return 0


ig.exit_handlers.append(exit_handler)
