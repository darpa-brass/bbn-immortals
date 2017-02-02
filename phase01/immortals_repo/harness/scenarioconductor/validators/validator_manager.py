import copy
import json
import time
from threading import Lock

from .. import threadprocessrouter as tpr
from ..data.base.scenarioapiconfiguration import ScenarioConductorConfiguration
from ..data.base.validationresults import TestResult, ValidationResults
from ..data.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from ..ll_api.data import AnalyticsEvent, Status

LOCAL_VALIDATORS = frozenset([
    'bandwidth-maximum-validator'
])


def _get_validator_by_identifier(identifier):
    if identifier == 'bandwidth-maximum-validator':
        return BandwidthValidator


# noinspection PyClassHasNoInit
class ValidatorInterface:
    def start(self):
        raise NotImplementedError

    def get_validator_name(self):
        """
        :rtype: str
        """
        raise NotImplementedError

    def process_event(self, event):
        """
        :type event: AnalyticsEvent
        """
        raise NotImplementedError

    def attempt_validation(self, terminal_state):
        """
        :type terminal_state: ValidationResults
        :rtype: TestResult
        """
        raise NotImplementedError


class BandwidthValidator(ValidatorInterface):
    """
    :type scenario_configuration: ScenarioConductorConfiguration
    :type offline_clients: list[str]
    :type validation_span_seconds: int
    :type state: TestResult
    """

    def __init__(self, scenario_configuration, client_identifier_list):
        self.offline_clients = client_identifier_list

        ibi = scenario_configuration.clients[0].imageBroadcastIntervalMS
        lsbi = scenario_configuration.clients[0].latestSABroadcastIntervalMS

        self.validation_span_seconds = long(ibi if ibi > lsbi else lsbi) / 1000
        self.validation_delay_seconds = self.validation_span_seconds / 2
        self.validation_span_seconds *= 10

        self._validation_start_time_s = None
        self._validation_bytes_transferred = 0
        self._pre_validation_bytes_transferred = 0

        self._resultant_bandwidth_kbits_per_second = None

        self._last_update_time_s = 0
        self._maximum_bandwidth_kbits_per_second = scenario_configuration.server.bandwidth
        self._lock = Lock()
        self.state = TestResult(
            validatorIdentifier=self.get_validator_name(),
            currentState=Status.PENDING,
            errorMessages=[],
            detailMessages=[]
        )

        self._image_send_count = 0
        self._image_receive_count = 0

    def process_event(self, event):
        """
        :type event: AnalyticsEvent
        """
        with self._lock:
            if self.state.currentState == Status.RUNNING:

                if self.validation_delay_seconds is None:
                    if event.type == 'FieldImageReceived':
                        self._image_receive_count += 1

                    if event.type == 'MyImageSent':
                        self._image_send_count += 1

                # Once every client has connected, start actual validation
                if event.eventSource in self.offline_clients:
                    self.offline_clients.remove(event.eventSource)

                elif len(self.offline_clients) == 0 and event.eventSource == 'global' \
                        and event.eventRemoteSource == 'global' and event.type == 'combinedServerTrafficBytes':

                    self._last_update_time_s = event.eventTime / 1000
                    self._validation_bytes_transferred = int(event.data)

                    if self._validation_start_time_s is None:
                        self._validation_start_time_s = self._last_update_time_s
                        self._pre_validation_bytes_transferred = self._validation_bytes_transferred
                        print "P0: " + str(time.time())

                    if self.validation_delay_seconds is not None:
                        if self._validation_start_time_s + self.validation_delay_seconds <= self._last_update_time_s:
                            print "P1: " + str(time.time())
                            self._validation_start_time_s = self._last_update_time_s
                            self.validation_delay_seconds = None

                    elif self._resultant_bandwidth_kbits_per_second is None \
                            and self._last_update_time_s - self._validation_start_time_s >= self.validation_span_seconds:
                        self._resultant_bandwidth_kbits_per_second = \
                            ((self._validation_bytes_transferred - self._pre_validation_bytes_transferred) * 8) / (
                                (self._last_update_time_s - self._validation_start_time_s) * 1000)

                        print 'PEB:'
                        print '     eventData:' + str(event.data)

                        print '     validationStartTimeS:' + str(self._validation_start_time_s)
                        print '     pre_validation_bytes_transferred:' + str(self._pre_validation_bytes_transferred)
                        print '     validation_bytes_transferred:' + str(
                            self._validation_bytes_transferred - self._pre_validation_bytes_transferred)
                        print '     lastUpdateTimeS:' + str(self._last_update_time_s)
                        print '     resultantBandwidth:' + str(self._resultant_bandwidth_kbits_per_second)
                        print '     validationSpanSeconds:' + str(self.validation_span_seconds)
                        print '     imagesReceived:' + str(self._image_receive_count)
                        print '     imagesSent:' + str(self._image_send_count)

    def start(self):
        with self._lock:
            self.state.currentState = Status.RUNNING

    def attempt_validation(self, terminal_state):
        with self._lock:

            validation_limit_reached = \
                (self._last_update_time_s - self._validation_start_time_s) > self.validation_span_seconds

            if validation_limit_reached:
                if len(self.offline_clients) > 0:
                    self.state.errorMessages.append("Not all clients have come online!")

                if self._resultant_bandwidth_kbits_per_second <= self._maximum_bandwidth_kbits_per_second:
                    self.state.currentState = Status.SUCCESS
                    self.state.detailMessages.append(
                        str(self._resultant_bandwidth_kbits_per_second) + ' <= ' + str(
                            self._maximum_bandwidth_kbits_per_second))

                else:
                    self.state.currentState = Status.FAILURE
                    self.state.errorMessages.append(
                        str(self._resultant_bandwidth_kbits_per_second) + ' > ' + str(
                            self._maximum_bandwidth_kbits_per_second))

            return self.state

    def get_validator_name(self):
        return 'bandwidth-maximum-validator'


class ValidatorManager:
    """
    :type _scenario_configuration: ScenarioConductorConfiguration
    :type _runner_configuration: ScenarioRunnerConfiguration
    :type _validator_identifiers: list[str]
    :type _client_identifiers: list[str]
    :type validators: list[ValidatorInterface]
    """

    def __init__(self, scenario_configuration, runner_configuration, validator_identifiers, client_identifiers):
        self._scenario_configuration = scenario_configuration
        self._runner_configuration = runner_configuration
        self._validator_identifiers = validator_identifiers
        self._client_identifiers = client_identifiers

        self.validators = []

        for i in validator_identifiers:
            v = _get_validator_by_identifier(i)(scenario_configuration, client_identifiers)
            self.validators.append(v)

        self.start_time_ms = None
        self.result = None

    def start(self):
        self.start_time_ms = time.time() / 1000
        for v in self.validators:
            v.start()

    def stop_or_wait(self, test_results):
        """
        :type test_results: ValidationResults
        """

        running_validators = []

        for v in self.validators:
            result = v.attempt_validation(True)

            if result.currentState == Status.RUNNING:
                running_validators.append(v)
            else:
                test_results.results.append(result)

        while len(running_validators) > 0:
            tpr.sleep(1)

            for v in copy.copy(running_validators):
                result = v.attempt_validation(True)
                if result.currentState != Status.RUNNING:
                    test_results.results.append(result)
                    running_validators.remove(v)

        with open(self._runner_configuration.deploymentDirectory + 'results/evaluation_result.json', 'w') as f:
            json.dump(test_results.to_dict(), f)

        self.result = test_results

    def process_event(self, event):
        """
        :type event: AnalyticsEvent
        """
        for v in self.validators:
            v.process_event(event)

            if event.type == 'Tooling_ValidationServerStopped' \
                    and event.dataType == 'mil.darpa.immortals.analytics.validators.result.ValidationResults':
                results = ValidationResults.from_json_str(event.data)
                self.stop_or_wait(results)
