# """
# Used to validate the behavior of the environment. It is currently very simple,
# and simply ensures all connected clients send their location at least once to
# all other connected clients.
# """

# import subprocess

import os
import signal
from threading import RLock

from . import immortalsglobals as ig
from . import threadprocessrouter as tpr
from .data.applicationconfig import ApplicationConfig
from .data.base.root_configuration import demo_mode
from .data.base.root_configuration import load_configuration
from .data.base.scenarioapiconfiguration import ScenarioConductorConfiguration
from .data.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from .ll_api.data import AnalyticsEvent
from .monitors.monitor_manager import MonitorManager
from .packages.subprocess32 import Popen
from .validators.validator_manager import ValidatorManager, LOCAL_VALIDATORS

demo_mode = load_configuration().visualizationConfiguration.enabled


class BehaviorValidator:
    """
    :type _process: Popen
    :type _validator_identifiers: set
    :type _runner_configuration: ScenarioRunnerConfiguration
    :type _scenario_configuration: ScenarioConductorConfiguration
    :type _client_identifiers: list[str]
    :type _validation_manager: ValidatorManager
    :type _monitor_manager: MonitorManager
    :type _duration: long
    """

    def __init__(self, runner_configuration, scenario_configuration):
        """
        :type runner_configuration: ScenarioRunnerConfiguration
        :type scenario_configuration: ScenarioConductorConfiguration
        """

        self._validator_identifiers = set(runner_configuration.scenario.validatorIdentifiers)

        self._runner_configuration = runner_configuration
        self._scenario_configuration = scenario_configuration

        self._client_identifiers = []

        for app in runner_configuration.scenario.deploymentApplications:  # type: ApplicationConfig
            if app.applicationIdentifier == 'ATAKLite' or app.applicationIdentifier == 'ataklite':
                self._client_identifiers.append(app.instanceIdentifier)

        self._lock = RLock()

        self._process = None

        self.ll_events = []

        client_count = 0
        max_wait_interval = 0
        for c in scenario_configuration.clients:
            client_count += c.count

            max_wait_interval = max(max_wait_interval, int(c.imageBroadcastIntervalMS),
                                    int(c.latestSABroadcastIntervalMS))

        calculated_duration = client_count * 8000 + max_wait_interval * 12 + 10000

        self._duration = max(calculated_duration, runner_configuration.minDurationMS)

        if demo_mode:
            ig.get_olympus().demo.validation_in_progress(
                'Validating environment. This will take approximately ' + str(self._duration / 1000) + ' seconds')

        py_validators = LOCAL_VALIDATORS.intersection(self._validator_identifiers)
        java_validators = self._validator_identifiers.difference(LOCAL_VALIDATORS)

        self._validation_manager = ValidatorManager(scenario_configuration=self._scenario_configuration,
                                                    runner_configuration=self._runner_configuration,
                                                    validator_identifiers=py_validators,
                                                    client_identifiers=self._client_identifiers)
        self._monitor_manager = MonitorManager(scenario_configuration=self._scenario_configuration,
                                               validator_identifiers=py_validators,
                                               listeners=[self._validation_manager.process_event, self.add_raw_events_for_ll])

        ci = []
        for identifier in self._client_identifiers:
            ci += ['-i', identifier]

        self._java_analytics_server_command = \
            ['java', '-jar', ig.configuration.validationProgram.executableFile, '-f',
             self._runner_configuration.deploymentDirectory + 'results/evaluation_event_log.txt',
             '-c',
             '--time-min-ms', str(self._duration),
             '--auxillary-logging-port', str(ig.configuration.testAdapter.port),
             'validate'] + ci + list(java_validators)

        self._server_running = False
        self._result = None

    def start_server(self):
        with self._lock:
            self._server_running = True

            if not os.path.exists(self._runner_configuration.deploymentDirectory + 'results/'):
                os.mkdir(self._runner_configuration.deploymentDirectory + 'results/')

            self._validation_manager.start()
            self._monitor_manager.start()

            std_endpoint = tpr.get_std_endpoint(ig.configuration.artifactRoot, 'analyticsLog4jServer')

            self._process = tpr.Popen(self._java_analytics_server_command, stdout=std_endpoint.out,
                                      stderr=std_endpoint.err)

    def add_raw_events_for_ll(self, event):
        """
        :type event: AnalyticsEvent
        """

        if event.eventSource == 'global' and event.type == 'combinedServerTrafficBytes':
            self.ll_events.append(event)

    def wait_for_validation_result(self):
        while self._process is not None and self._process.poll() is None and self._validation_manager.result is None:
            tpr.sleep(1)

        self._monitor_manager.stop()

        self._result = self._validation_manager.result

        if demo_mode:
            ig.get_olympus().demo.update_validation_status(self._result)

        return self._result

    def stop(self):
        with self._lock:

            if self._server_running:

                if self._process is not None and self._process.poll() is None:
                    self._process.send_signal(signal.SIGINT)
                    self._process.wait()

                self._server_running = False
