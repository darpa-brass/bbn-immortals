# """
# Used to validate the behavior of the environment. It is currently very simple,
# and simply ensures all connected clients send their location at least once to
# all other connected clients.
# """

# import subprocess

import json
import os
import signal
from threading import Thread, Timer

from configurationmanager import Configuration
from packages import subprocess32 as subprocess


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
        self.time_limit_seconds = runner_configuration.timeout
        self.validation_running = True

    def start(self, finishlistener=None):
        ci = []

        for identifier in self.client_identifiers:
            ci += ['-i', identifier]

        command = ['java', '-jar', Configuration.validation_program.executable_filepath, '-f',
                   self.deployment_path + 'results/evaluation_event_log.txt', '-c',
                   'validate'] + ci + self.validator_identifiers

        if not os.path.exists(self.deployment_path + 'results/'):
            os.mkdir(self.deployment_path + 'results/')

        self.process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        self.listener = finishlistener

        self.monitoringthread = Thread(None, self.monitoroutput, None)
        self.monitoringthread.start()

        self.timer = Timer(self.time_limit_seconds, self.stop, ())
        self.timer.start()

    def stop(self):
        if self.validation_running:
            self.validation_running = False
            self.timer.cancel()
            self.process.send_signal(signal.SIGINT);

    def monitoroutput(self):
        while True:
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

            self.listener(data)
            self.stop()

        return None


def main():
    bv = BehaviorValidator(None, 600)
    bv.start()


if __name__ == '__main__':
    main()
