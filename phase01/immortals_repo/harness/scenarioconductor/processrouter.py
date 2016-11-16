import logging
from threading import Thread

_running_threads = {}
_shutdown_methods = {}
_shutdown_args = {}
_keep_running = True


def _exit_handler():
    print "existing pr"
    for identifier in _shutdown_methods:
        _shutdown_methods[identifier](*_shutdown_args[identifier])

    _shutdown_methods.clear()
    _shutdown_args.clear()
    global _keep_running
    _keep_running = False


class ProcessRouter:

    @staticmethod
    def keep_running():
        global _keep_running
        return _keep_running

    @staticmethod
    def _thread_executor(method, args=()):
        try:
            method(*args)

        except Exception as e:
            _exit_handler()
            raise e

    @staticmethod
    def exit_handler():
        _exit_handler()

    @staticmethod
    def start_thread(identifier, thread_method, shutdown_method=None, thread_args=(), shutdown_args=()):
        if identifier in _running_threads:
            logging.fatal('A process with the identifier \'' + identifier + '\' already exists in the ProcessRouter!')

        args = [thread_method, thread_args]
        # args = [ProcessRouter.throw_exception]
        thread = Thread(target=ProcessRouter._thread_executor, args=args)
        # thread = Thread(target=ProcessRouter._thread_executor, args=args)
        thread.daemon = True
        # thread = Thread(target=thread_method, args=thread_args)
        _running_threads[identifier] = thread
        _shutdown_methods[identifier] = shutdown_method
        _shutdown_args[identifier] = shutdown_args
        thread.start()
