#!/usr/bin/env python

import logging
import os
import shutil
import signal
import subprocess

from utils import replace

_ID_JAR_FILEPATH = '$JAR_FILEPATH!'

_CMD_START_JAR = ('java', '-jar', _ID_JAR_FILEPATH)

_running_processes = {}


def _exit_handler(signal, frame):
    for process in _running_processes.values():
        process.terminate()

signal.signal(signal.SIGINT, _exit_handler)


class JavaPlatform:

    def __init__(self, execution_path, application_configuration, wipe_existing):
        self.identifier = application_configuration.instance_identifier
        self.java_environment_identifier =  application_configuration.deployment_platform_environment
        self.wipe_existing = wipe_existing
        self.runtime_path = execution_path

        if self.java_environment_identifier == 'java_local':
            print "Using local Java environment..."
        else:
            raise Exception("A valid java environment and identifier must be provided!")

    def call(self, call_list):
        logging.debug('EXEC: ' + call_list)
        return subprocess.call(call_list)


    def check_output(self, call_list):
        logging.debug('EXEC: ' + call_list)
        return subprocess.check_output(call_list)


    def Popen(self, args, bufsize=0, executable=None, stdin=None, stdout=None, stderr=None, preexec_fn=None, close_fds=False, shell=False, cwd=None, env=None, universal_newlines=False, startupinfo=None, creationflags=0):
        logging.debug('EXEC: ' + args)
        return subprocess.Popen(args, bufsize, executable,  stdin, stdout, stderr, preexec_fn, close_fds, shell, cwd, env, universal_newlines, startupinfo, creationflags)


    def platform_setup(self):
        logging.debug('Setting up ' + self.java_environment_identifier + ' for ' + self.identifier + ' with wipe_existing=' + str(self.wipe_existing))

        if self.wipe_existing:
            if os.path.exists(self.runtime_path):
                shutil.rmtree(self.runtime_path)

        os.makedirs(self.runtime_path)


    def platform_teardown(self):
        pass

    def copy_file(self, source_file_location, target_file_location):
        logging.debug('Copying ' + source_file_location + ' to ' + target_file_location + '.')
        shutil.copyfile(source_file_location, target_file_location)

    def start_jar(self, jar_path, stdout_target, stderr_target):
        logging.debug('Starting ' + jar_path + ' for ' + self.identifier)
        call_array = list(_CMD_START_JAR)
        replace(call_array, _ID_JAR_FILEPATH, jar_path)
        logging.debug('EXEC: ' + str(call_array))


        new_process = subprocess.Popen(
        args=call_array,
        cwd=self.runtime_path,
        stdout=stdout_target,
        stderr=stderr_target
        )

        _running_processes[self.identifier] = new_process
        return new_process

    def force_stop_process(self, identifier):
        logging.debug('Killing process for ' + identifier + '.')
        ending_process = _running_processes.pop(identifier)
        ending_process.terminate()
