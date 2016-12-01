# """
# Used to validate the behavior of the environment. It is currently very simple,
# and simply ensures all connected clients send their location at least once to
# all other connected clients.
# """

# import subprocess

import os
import signal

import threadprocessrouter as tpr
from configurationmanager import Configuration
from data.validationresult import ValidationResult
from packages import commentjson as json


class BehaviorValidator:
    def __init__(self, runner_configuration):
        self.validator_identifiers = runner_configuration.scenario.validator_identifiers

        self.client_identifiers = []

        for app in runner_configuration.scenario.deployment_applications:  # type: ApplicationConfig
            if app.application_identifier == 'ATAKLite' or app.application_identifier == 'ataklite':
                self.client_identifiers.append(app.instance_identifier)

        self.deployment_path = runner_configuration.deployment_directory
        self.process = None
        self.listener = None
        self.monitoringthread = None;
        self.timer = None
        self.time_limit_ms = runner_configuration.scenario.durationMS if runner_configuration.scenario.durationMS > runner_configuration.minDurationMS else runner_configuration.minDurationMS
        self.minimum_run_time_ms = runner_configuration.minDurationMS
        self.validation_running = True

    def start(self, finishlistener=None):
        if len(self.client_identifiers) > 0:
            ci = []
            for identifier in self.client_identifiers:
                ci += ['-i', identifier]

            command = ['java', '-jar', Configuration.validation_program.executable_filepath, '-f',
                       self.deployment_path + 'results/evaluation_event_log.txt', '-c',
                       '--time-min-ms', str(self.minimum_run_time_ms),
                       'validate'] + ci + self.validator_identifiers

        else:
            command = ['java', '-jar', Configuration.validation_program.executable_filepath, '-f',
                       self.deployment_path + 'results/evaluation_event_log.txt',
                       '--time-max-ms', str(self.time_limit_ms),
                       '--time-min-ms', str(self.minimum_run_time_ms),
                       'observe']

        if not os.path.exists(self.deployment_path + 'results/'):
            os.mkdir(self.deployment_path + 'results/')

        self.process = tpr.Popen(command, stdout=tpr.PIPE, stderr=tpr.PIPE)
        self.listener = finishlistener

        tpr.start_thread(thread_method=BehaviorValidator.monitoroutput, thread_args=[self])

        self.timer = tpr.start_timer(duration_seconds=self.time_limit_ms/1000, shutdown_method=self.stop,
                                     halt_on_shutdown=False)

    def stop(self):
        if self.validation_running:
            self.validation_running = False
            self.timer.cancel()
            self.process.send_signal(signal.SIGINT)

    def monitoroutput(self):
        result = None

        while result is None:
            try:
                value = self.process.stdout.readline()
                if value != '':
                    result = self.analyzeinput(value)

            except OSError:
                pass

    def analyzeinput(self, string_value):
        data = json.loads(string_value)

        if data['type'] == 'Tooling_ValidationFinished':
            result = json.loads(data['data'])
            print data['data']

            with open(self.deployment_path + 'results/evaluation_result.json', 'w') as f:
                json.dump(result, f)

            validation_result = ValidationResult.from_dict(result)
            self.listener(validation_result)
            self.stop()
            return validation_result

        return None


def main():
    bv = BehaviorValidator(None, 600)
    bv.start()


if __name__ == '__main__':
    main()
