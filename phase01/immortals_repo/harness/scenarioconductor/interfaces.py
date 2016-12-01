import threadprocessrouter as tpr


class CommandHandlerInterface:
    def __init__(self, command_processor=tpr, halt_on_shutdown=None, shutdown_method=None, shutdown_args=None,
                 stdout=None,
                 stderr=None):
        self.command_processor = command_processor
        self.halt_on_shutdown = halt_on_shutdown
        self.shutdown_method = shutdown_method
        self.shutdown_args = shutdown_args
        self.stdout = stdout
        self.stderr = stderr

    def call(self, args, halt_on_shutdown=None, shutdown_method=None, shutdown_args=(), stdout=None, stderr=None,
             *popenargs, **kwargs):

        if halt_on_shutdown is None and self.halt_on_shutdown is not None:
            halt_on_shutdown = self.halt_on_shutdown

        if shutdown_method is None and self.shutdown_method is not None:
            shutdown_method = self.shutdown_method

        if shutdown_args is None and self.shutdown_args is not None:
            shutdown_args = self.shutdown_args

        if stdout is None and self.stdout is not None:
            stdout = self.stdout

        if stderr is None and self.stderr is not None:
            stderr = self.stderr

        return self.command_processor.call(args=args, halt_on_shutdown=halt_on_shutdown,
                                           shutdown_method=shutdown_method, shutdown_args=shutdown_args, stdout=stdout,
                                           stderr=stderr, *popenargs, **kwargs)

    def check_call(self, args, halt_on_shutdown=None, shutdown_method=None, shutdown_args=(), stdout=None, stderr=None,
                   *popenargs, **kwargs):

        if halt_on_shutdown is None and self.halt_on_shutdown is not None:
            halt_on_shutdown = self.halt_on_shutdown

        if shutdown_method is None and self.shutdown_method is not None:
            shutdown_method = self.shutdown_method

        if shutdown_args is None and self.shutdown_args is not None:
            shutdown_args = self.shutdown_args

        if stdout is None and self.stdout is not None:
            stdout = self.stdout

        if stderr is None and self.stderr is not None:
            stderr = self.stderr

        return self.command_processor.check_call(args=args, halt_on_shutdown=halt_on_shutdown,
                                                 shutdown_method=shutdown_method, shutdown_args=shutdown_args,
                                                 stdout=stdout, stderr=stderr, *popenargs, **kwargs)

    def check_output(self, args, halt_on_shutdown=None, shutdown_method=None, shutdown_args=(), stderr=None, *popenargs,
                     **kwargs):

        if halt_on_shutdown is None and self.halt_on_shutdown is not None:
            halt_on_shutdown = self.halt_on_shutdown

        if shutdown_method is None and self.shutdown_method is not None:
            shutdown_method = self.shutdown_method

        if shutdown_args is None and self.shutdown_args is not None:
            shutdown_args = self.shutdown_args

        if stderr is None and self.stderr is not None:
            stderr = self.stderr

        return self.command_processor.check_output(args=args, halt_on_shutdown=halt_on_shutdown,
                                                   shutdown_method=shutdown_method, shutdown_args=shutdown_args,
                                                   stderr=stderr, *popenargs, **kwargs)

    # noinspection PyPep8Naming
    def Popen(self, args, halt_on_shutdown=None, shutdown_method=None, shutdown_args=(), stdout=None, stderr=None,
              *popenargs, **kwargs):

        if halt_on_shutdown is None and self.halt_on_shutdown is not None:
            halt_on_shutdown = self.halt_on_shutdown

        if shutdown_method is None and self.shutdown_method is not None:
            shutdown_method = self.shutdown_method

        if shutdown_args is None and self.shutdown_args is not None:
            shutdown_args = self.shutdown_args

        if stdout is None and self.stdout is not None:
            stdout = self.stdout

        if stderr is None and self.stderr is not None:
            stderr = self.stderr

        return self.command_processor.Popen(args=args, halt_on_shutdown=halt_on_shutdown,
                                            shutdown_method=shutdown_method, shutdown_args=shutdown_args, stdout=stdout,
                                            stderr=stderr, *popenargs, **kwargs)
