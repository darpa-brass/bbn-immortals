#!/usr/bin/env python3

import os
import re
import subprocess
import time

from odb.datatypes import STARTUP_TIMEOUT_S, SHUTDOWN_TIMEOUT_S, STDOUT_LOG_BASE_NAME, \
    STDERR_LOG_BASE_NAME

ODB_STARTED_REGEX = r'[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3} INFO  OrientDB Server is active.*'

XML_COMBINED_FILE = 'mdl_combined.xml'


class OdbStarter:
    def __init__(self, host: str, port: int, orientdb_home: str, orientdb_root_password: str):
        self.host = host
        self.port = port
        self.orientdb_home = orientdb_home
        self.orientdb_root_password = orientdb_root_password
        self.start_timestamp = time.strftime('%Y%m%d_%H%M%S')
        self._odb_process = None
        self._odb_stdout = None
        self._odb_stderr = None

    def start_db(self):
        self._odb_stdout = open(STDOUT_LOG_BASE_NAME, 'w', 1)
        self._odb_stderr = open(STDERR_LOG_BASE_NAME, 'w', 1)

        print('Starting OrientDB. See "' + STDOUT_LOG_BASE_NAME + "' and '"
              + STDERR_LOG_BASE_NAME + "' for logs.")

        self._odb_process = subprocess.Popen(
            ['bash', os.path.join(self.orientdb_home, 'bin', 'server.sh')],
            env={
                'ORIENTDB_ROOT_PASSWORD': self.orientdb_root_password
            },
            stdout=self._odb_stdout,
            stderr=self._odb_stderr
        )

        read_file = open(STDERR_LOG_BASE_NAME, 'r')

        wait_time_seconds = STARTUP_TIMEOUT_S
        while wait_time_seconds > 0:
            for line in read_file.readlines():

                if re.match(ODB_STARTED_REGEX, line) is not None:
                    print("OrientDB has been started successfully.")
                    return

            time.sleep(0.2)
            wait_time_seconds = wait_time_seconds - 0.2

        print("FAILED TO START ORIENTDB within the expected time!")
        read_file.close()
        self._odb_process.kill()

        print('Finished starting OrientDB')

    def wait(self):
        if self._odb_process is not None:
            self._odb_process.wait()

    def stop(self):
        if self._odb_process is not None:
            print('Shutting down OrientDB...')

            expiration = SHUTDOWN_TIMEOUT_S

            self._odb_process.terminate()

            while expiration > 0:
                status = self._odb_process.poll()

                if status is None:
                    time.sleep(0.2)
                    expiration = expiration - 0.2

                else:
                    print('OrientDB has been successfully shut down.')
                    return

            print("OrientDB not shutting down gracefully. Killing it...")
            self._odb_process.kill()
