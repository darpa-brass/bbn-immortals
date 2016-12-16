# """
# Used to validate the behavior of the environment. It is currently very simple,
# and simply ensures all connected clients send their location at least once to
# all other connected clients.
# """

# import subprocess

import os
import signal
from threading import RLock

from . import threadprocessrouter as tpr
from .data.applicationconfig import ApplicationConfig
from .data.validationresult import ValidationResult
from .immortalsglobals import configuration
from .packages import commentjson as json
from .packages.subprocess32 import Popen


class BehaviorValidator:
    """
    :type process: Popen
    """

    def __init__(self, runner_configuration):
        self.validator_identifiers = runner_configuration.scenario.validatorIdentifiers

        self.client_identifiers = []

        for app in runner_configuration.scenario.deploymentApplications:  # type: ApplicationConfig
            if app.applicationIdentifier == 'ATAKLite' or app.applicationIdentifier == 'ataklite':
                self.client_identifiers.append(app.instanceIdentifier)

        self._lock = RLock()

        self.deployment_path = runner_configuration.deploymentDirectory
        self.process = None
        self.monitor = None
        if runner_configuration.scenario.durationMS > runner_configuration.minDurationMS:
            self.time_limit_ms = runner_configuration.scenario.durationMS
        else:
            self.time_limit_ms = runner_configuration.minDurationMS

        self.minimum_run_time_ms = runner_configuration.minDurationMS
        self.server_running = False
        self.result = None

    def start_server(self):
        with self._lock:
            self.server_running = True
            if len(self.client_identifiers) > 0:
                ci = []
                for identifier in self.client_identifiers:
                    ci += ['-i', identifier]

                command = ['java', '-jar', configuration.validationProgram.executableFile, '-f',
                           self.deployment_path + 'results/evaluation_event_log.txt', '-c',
                           '--time-min-ms', str(self.minimum_run_time_ms),
                           'validate'] + ci + self.validator_identifiers

            else:
                command = ['java', '-jar', configuration.validationProgram.executableFile, '-f',
                           self.deployment_path + 'results/evaluation_event_log.txt',
                           '--time-max-ms', str(self.time_limit_ms),
                           '--time-min-ms', str(self.minimum_run_time_ms),
                           'observe']

            if not os.path.exists(self.deployment_path + 'results/'):
                os.mkdir(self.deployment_path + 'results/')

            self.process = tpr.Popen(command, stdout=tpr.PIPE, stderr=tpr.PIPE)
            self.monitor = tpr.start_thread(thread_method=BehaviorValidator.monitoroutput, thread_args=[self])

    def wait_for_validation_result(self):
        counter = 0
        duration = self.time_limit_ms / 1000

        while counter < duration and self.server_running:
            tpr.sleep(1)
            counter += 1

        with self._lock:
            if self.server_running:
                self.stop()

        return self.result

    def stop(self):
        with self._lock:

            if self.server_running:

                if self.process is not None and self.process.poll() is None:
                    self.process.send_signal(signal.SIGINT)
                    self.process.wait()

                self.server_running = False

    def monitoroutput(self):
        result = None

        while result is None:
            try:
                value = self.process.stdout.readline()
                if value != '':

                    data = json.loads(value)

                    if data['type'] == 'Tooling_ValidationFinished':
                        result = json.loads(data['data'])
                        print data['data']

                        with open(self.deployment_path + 'results/evaluation_result.json', 'w') as f:
                            json.dump(result, f)

                        validation_result = ValidationResult.from_dict(result)
                        result = validation_result
                        self.result = validation_result
                        self.stop()

            except OSError:
                pass
