import logging
import time
from threading import RLock
from typing import List, Set, Type

from pymmortals import immortalsglobals as ig
from pymmortals import triples_helper
from pymmortals.datatypes.interfaces import AbstractMonitor
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.routing import EventType, EventTags, EventTag
from pymmortals.datatypes.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.analyticsevent import AnalyticsEvent
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.status import Status
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testresult import TestResult
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validators import Validators
from pymmortals.monitors.server_network_traffic_monitor import ServerNetworkTrafficMonitor
from pymmortals.validators.abstract_local_validator import AbstractLocalValidator

_event_ticker_lock = RLock()
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


class BandwidthValidator(AbstractLocalValidator):
    @classmethod
    def identifier(cls) -> str:
        return Validators.BANDWIDTH_MAXIMUM_VALIDATOR.identifier

    @classmethod
    def get_monitor_classes(cls) -> Set[Type[AbstractMonitor]]:
        return {ServerNetworkTrafficMonitor}

    def run_time_ms(self) -> int:
        return self._run_time_ms

    def __init__(self, gif: GmeInterchangeFormat, runner_configuration: ScenarioRunnerConfiguration):
        super().__init__(gif=gif, runner_configuration=runner_configuration)
        self._offline_clients: List[str] = [a.instanceIdentifier
                                            for a in runner_configuration.scenario.deploymentApplications
                                            if a.applicationIdentifier.lower() == 'ataklite']
        self._timestamp_list: List[int] = []
        self._byte_list: List[int] = []

        self._max_hit_bandwidth_kilobits_per_second: int = -1

        validation_reporting_interval_secs = \
            get_configuration().validation.bandwidthMonitorReportingIntervalMS / 1000

        self._sample_span_seconds: int = 0

        client_count = triples_helper.get_android_client_count(gif)
        image_broadcast_interval_ms = triples_helper.get_image_rate_ms(gif)
        pli_broadcast_interval_ms = triples_helper.get_pli_rate_ms(gif)

        for q in range(client_count):
            self._sample_span_seconds = \
                max(self._sample_span_seconds, image_broadcast_interval_ms, pli_broadcast_interval_ms)

        self._sample_span_seconds /= 1000
        sample_delay_seconds = max(len(self._offline_clients) * 2, self._sample_span_seconds // 2)

        self._sample_span_seconds *= get_configuration().validation.bandwidthValidatorSampleDurationMultiplier

        # Wait for half the sample span for things to settle before starting to measure data
        self._lower_idx: int = sample_delay_seconds
        self._upper_idx: int = int((self._lower_idx + (self._sample_span_seconds / validation_reporting_interval_secs)))

        logging.debug('Bandwidth Sampling Starting Lower Idx: ' + str(self._lower_idx))
        logging.info('Bandwidth Sampling Starting Upper Idx: ' + str(self._upper_idx))

        self._maximum_bandwidth_kbits_per_second: int = triples_helper.get_bandwidth_constraint_kbit_per_second(gif)

        self._image_send_count = 0
        self._image_receive_count = 0
        self._run_time_ms = max(60, 3 * self._sample_span_seconds)

    def _receive_event(self, event_tag: EventTag, data: AnalyticsEvent):
        if isinstance(data, list):
            data_list = data
        else:
            data_list = [data]

        for event in data_list:
            if self._current_result.currentState == Status.RUNNING:

                if event_tag == EventTags.AnalyticsEventServerNetworkTrafficMeasuredBytes:

                    bandwidth_calculated_event = None

                    if len(self._offline_clients) == 0:
                        self._timestamp_list.append(event.eventTime)
                        self._byte_list.append(int(event.data))
                        logging.debug("Bandwidth Timestamp: " + str(event.eventTime))
                        logging.debug("Bandwidth Total Bytes: " + str(event.data))

                        if self._upper_idx < len(self._timestamp_list):
                            time_delta = self._timestamp_list[self._upper_idx] - self._timestamp_list[
                                self._lower_idx]
                            bytes_delta = self._byte_list[self._upper_idx] - self._byte_list[self._lower_idx]

                            logging.debug("Bandwith Window Bytes Delta: " + str(bytes_delta))
                            logging.debug('Bandwidth Window Time Delta: ' + str(time_delta))

                            bandwidth_kilobits_per_second = int((bytes_delta * 8 / 1000) / (time_delta / 1000))
                            logging.debug('Bandwidth Kilobits Per Second: ' + str(bandwidth_kilobits_per_second))
                            self._max_hit_bandwidth_kilobits_per_second = \
                                max(self._max_hit_bandwidth_kilobits_per_second,
                                    bandwidth_kilobits_per_second)

                            if bandwidth_kilobits_per_second >= self._maximum_bandwidth_kbits_per_second:
                                self._current_result.errorMessages.append(
                                    str(bandwidth_kilobits_per_second)
                                    + ' is greater than the maximum bandwidth of '
                                    + str(self._maximum_bandwidth_kbits_per_second) + '!')

                            self._lower_idx += 1
                            self._upper_idx += 1

                            bandwidth_calculated_event = _create_bandwidth_calculated_event(
                                (bandwidth_kilobits_per_second / 8 * 1000),
                                event_time_ms=event.eventTime)

                    if bandwidth_calculated_event is not None:
                        ig.get_event_router().submit_asynchronously(
                            EventTags.AnalyticsEventServerNetworkTrafficCalculatedBytesPerSec,
                            bandwidth_calculated_event)

                elif event_tag.event_type == EventType.ANALYSIS and event.eventSource in self._offline_clients:
                    self._offline_clients.remove(event.eventSource)

    def _attempt_validation(self, terminal_state: bool) -> TestResult:
        if terminal_state:
            if len(self._offline_clients) > 0:
                self._current_result.errorMessages.append("Not all clients have come online!")

            if self._max_hit_bandwidth_kilobits_per_second <= 0:
                self._current_result.errorMessages.append(
                    "Not enough time has passed to collect an accurate bandwidth measurement!")

            if len(self._current_result.errorMessages) > 0:
                self._current_result.currentState = Status.FAILURE

            else:
                self._current_result.detailMessages.append(
                    'The maximum bandwith over a ' + str(self._sample_span_seconds) +
                    ' second window is '
                    + str(self._max_hit_bandwidth_kilobits_per_second)
                    + 'kilobits per second, which is below the threshold of '
                    + str(self._maximum_bandwidth_kbits_per_second) + '.')
                self._current_result.currentState = Status.SUCCESS

            return self._current_result
