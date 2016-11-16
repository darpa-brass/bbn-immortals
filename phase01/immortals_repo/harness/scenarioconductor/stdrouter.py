import atexit
import os
import sys


def _exit_handler():
    for endpoint in StdRouter.endpoints.values():
        endpoint.out.flush()
        endpoint.err.flush()


atexit.register(_exit_handler)


class StdEndpointSet:

    def __init__(self, std_dirpath=None):
        if std_dirpath is not None:
            self.err = open(os.path.join(std_dirpath, 'stderr.txt'), 'w')
            self.out = open(os.path.join(std_dirpath, 'stdout.txt'), 'w')
        else:
            self.err = sys.stderr
            self.out = sys.stdout

        pass


class StdRouter:

    endpoints = {}

    @staticmethod
    def get_endpoint(execution_dirpath):
        if execution_dirpath not in StdRouter.endpoints:
            StdRouter.endpoints[execution_dirpath] = StdEndpointSet(execution_dirpath)

        return StdRouter.endpoints[execution_dirpath]
