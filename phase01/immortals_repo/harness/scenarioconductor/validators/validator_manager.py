import copy
import json
import logging
import time
from threading import Lock

from .. import immortalsglobals as ig
from .. import threadprocessrouter as tpr
from ..data.base.scenarioapiconfiguration import ScenarioConductorConfiguration
from ..data.base.validationresults import TestResult, ValidationResults
from ..data.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from ..ll_api.data import AnalyticsEvent, Status

LOCAL_VALIDATORS = frozenset([
    'bandwidth-maximum-validator'
])

_event_ticker_lock = Lock()
_event_ticker = 0


def _create_bandwidth_calculated_event(bytes_used, event_time_ms=None):
    global _event_ticker, _event_ticker_lock

    with _event_ticker_lock:
        if event_time_ms is None:
            event_time_ms = time.time() * 1000

        event = AnalyticsEvent(
            type='combinedServerTrafficBytesPerSecond',
            eventSource='bandwidth-maximum-validator',
            eventTime=event_time_ms,
            eventRemoteSource='global',
            dataType='java.lang.Long',
            eventId=_event_ticker,
            data=str(bytes_used)
        )
        _event_ticker += 1
        return event


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
        :type terminal_state: bool
        :rtype: TestResult
        """
        raise NotImplementedError


class BandwidthValidator(ValidatorInterface):
    """
    :type scenario_configuration: ScenarioConductorConfiguration
    :type _offline_clients: list[str]
    :type state: TestResult
    """

    def __init__(self, scenario_configuration, client_identifier_list, listeners):
        self._offline_clients = list(client_identifier_list)
        self._online_client_image_count = {k: 0 for k in client_identifier_list}
        self._online_client_latest_sa_count = {k: 0 for k in client_identifier_list}
        self._timestamp_list = []
        self._byte_list = []
        self._listeners = listeners

        self._max_hit_bandwidth_kilobits_per_second = -1

        validation_reporting_interval_secs = \
            ig.configuration.validation.bandwidthMonitorReportingIntervalMS / 1000

        self._sample_span_seconds = 0
        for c in scenario_configuration.clients:
            self._sample_span_seconds = \
                max(self._sample_span_seconds, int(c.imageBroadcastIntervalMS), int(c.latestSABroadcastIntervalMS))

        self._sample_span_seconds *= ig.configuration.validation.bandwidthValidatorSampleDurationMultiplier
        self._sample_span_seconds /= 1000

        # Wait for half the sample span for things to settle before starting to measure data
        self._lower_idx = self._sample_span_seconds / 2
        self._upper_idx = (self._lower_idx + (self._sample_span_seconds / validation_reporting_interval_secs))

        logging.debug('Bandwidth Sampling Starting Lower Idx: ' + str(self._lower_idx))
        logging.debug('Bandwidth Sampling Starting Upper Idx: ' + str(self._upper_idx))

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

                if len(self._offline_clients) == 0 and event.eventSource == 'server-network-traffic-monitor' \
                        and event.eventRemoteSource == 'global' and event.type == 'combinedServerTrafficBytes':

                    self._timestamp_list.append(event.eventTime)
                    self._byte_list.append(long(event.data))

                    if self._upper_idx < len(self._timestamp_list):
                        time_delta = self._timestamp_list[self._upper_idx] - self._timestamp_list[
                            self._lower_idx]
                        bytes_delta = self._byte_list[self._upper_idx] - self._byte_list[self._lower_idx]

                        bandwidth_kilobits_per_second = ((bytes_delta * 8 / 1000) / (time_delta / 1000))
                        self._max_hit_bandwidth_kilobits_per_second = \
                            max(self._max_hit_bandwidth_kilobits_per_second,
                                bandwidth_kilobits_per_second)

                        if bandwidth_kilobits_per_second >= self._maximum_bandwidth_kbits_per_second:
                            self.state.errorMessages.append(str(bandwidth_kilobits_per_second)
                                                            + ' is greater than the maximum bandwidth of '
                                                            + str(
                                self._maximum_bandwidth_kbits_per_second) + '!')

                        self._lower_idx += 1
                        self._upper_idx += 1

                        bandwidth_calculated_event = _create_bandwidth_calculated_event(
                            (bandwidth_kilobits_per_second / 8 * 1000),
                            event_time_ms=event.eventTime)

                        for l in self._listeners:
                            l(bandwidth_calculated_event)

                elif event.eventSource in self._offline_clients:
                    self._offline_clients.remove(event.eventSource)

    def start(self):
        with self._lock:
            self.state.currentState = Status.RUNNING

    def attempt_validation(self, terminal_state):
        with self._lock:

            if terminal_state:
                if len(self._offline_clients) > 0:
                    self.state.errorMessages.append("Not all clients have come online!")

                if self._max_hit_bandwidth_kilobits_per_second <= 0:
                    self.state.errorMessages.append(
                        "Not enough time has passed to collect an accurate bandwidth measurement!")

                if len(self.state.errorMessages) > 0:
                    self.state.currentState = Status.FAILURE

                else:
                    self.state.detailMessages.append('The maximum bandwith over a ' + str(self._sample_span_seconds) +
                                                     ' second window is '
                                                     + str(self._max_hit_bandwidth_kilobits_per_second)
                                                     + ' kilobits per second, which is below the threshold of '
                                                     + str(self._maximum_bandwidth_kbits_per_second) + '.')
                    self.state.currentState = Status.SUCCESS

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

    def __init__(self, scenario_configuration, runner_configuration, validator_identifiers, client_identifiers,
                 listeners):
        self._scenario_configuration = scenario_configuration
        self._runner_configuration = runner_configuration
        self._validator_identifiers = validator_identifiers
        self._client_identifiers = client_identifiers
        self._listeners = listeners

        self.validators = []

        for i in validator_identifiers:
            v = _get_validator_by_identifier(i)(scenario_configuration, client_identifiers, self._listeners)
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
