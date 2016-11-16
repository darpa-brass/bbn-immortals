# """
# Used to validate the behavior of the environment. It is currently very simple,
# and simply ensures all connected clients send their location at least once to
# all other connected clients.
# """

# import subprocess

import json
import signal
from threading import Thread, Timer

from configurationmanager import ApplicationConfig, Configuration
from packages import subprocess32 as subprocess


class BehaviorValidator:
    def __init__(self, runner_configuration):
        self.validator_identifiers = runner_configuration.scenario.validator_identifiers

        self.client_identifiers = []

        for app in runner_configuration.scenario.deployment_applications:  #  type: ApplicationConfig
            if app.application_identifier == 'ATAKLite' or app.application_identifier == 'ataklite':
                self.client_identifiers.append(app.instance_identifier)

        self.deployment_path = runner_configuration.deployment_directory
        self.process = None
        self.time_limit_seconds = runner_configuration.timeout
        self.validation_running = True

    def start(self, finishlistener=None):
        ci = []

        for identifier in self.client_identifiers:
            ci += ['-i', identifier]

        command = ['java', '-jar', Configuration.validation_program.executable_filepath, '-f',
                   self.deployment_path + 'validation_log.txt', '-c',
                   'validate'] + ci + self.validator_identifiers
        self.process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        self.listener = finishlistener

        self.monitoringthread = Thread(None, self.monitoroutput, None)
        self.monitoringthread.start()

        self.timeout = Timer(self.time_limit_seconds, self.stop, ())
        self.timeout.start()

    def stop(self):
        if self.validation_running:
            self.validation_running = False
            self.timeout.cancel()
            self.process.send_signal(signal.SIGINT);

    def monitoroutput(self):
        while True:
            try:
                value = self.process.stdout.readline()
                if value == '':
                    break;

                result = self.analyzeinput(value)
                if result is not None:
                    print result
                    self.listener(result)
                    self.stop()

            except OSError:
                pass

    def analyzeinput(self, string_value):
        data = json.loads(string_value)

        if data['type'] == 'Tooling_ValidationFinished':
            results = data['data']

            self.stop()
            return json.dumps(results)

        return None

def main():
    bv = BehaviorValidator(None, 600)
    bv.start()


if __name__ == '__main__':
    main()
