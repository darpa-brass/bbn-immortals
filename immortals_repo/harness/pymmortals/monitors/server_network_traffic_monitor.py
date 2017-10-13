import pcapy
import time
from threading import Lock

from pymmortals import threadprocessrouter as tpr, immortalsglobals as ig
from pymmortals.datatypes.interfaces import AbstractMonitor
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.routing import EventTags
from pymmortals.datatypes.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.analyticsevent import AnalyticsEvent

_event_ticker_lock = Lock()
_event_ticker = 0


def _create_bytes_used_event(bytes_used, event_time_ms=None):
    global _event_ticker, _event_ticker_lock

    with _event_ticker_lock:
        if event_time_ms is None:
            event_time_ms = time.time() * 1000

        event = AnalyticsEvent(
            type='combinedServerTrafficBytes',
            eventSource='server-network-traffic-monitor',
            eventTime=event_time_ms,
            eventRemoteSource='global',
            dataType='java.lang.Long',
            eventId=_event_ticker,
            data=str(bytes_used)
        )
        _event_ticker += 1
        return event


# noinspection PyMissingConstructor
class ServerNetworkTrafficMonitor(AbstractMonitor):
    @classmethod
    def identifier(cls) -> str:
        return 'server-network-traffic-monitor'

    def __init__(self, gif: GmeInterchangeFormat, runner_configuration: ScenarioRunnerConfiguration):
        super().__init__(gif, runner_configuration)
        self._start_time_secs: int = None
        self._last_polling_time_secs: int = None
        self._running_bytes_transferred: int = 0
        self._reader = None
        self._running: bool = False
        self._lock: Lock = Lock()
        self._duration: int = -1

    def is_running(self):
        return self._running

    def start(self, duration_ms):
        self._running = True
        self._start_time_secs = time.time()
        self._duration = duration_ms
        self._reader = pcapy.open_live(get_configuration().validation.pcapyMonitorInterface,
                                       get_configuration().validation.pcapySnapshotLength,
                                       get_configuration().validation.pcapyPromiscuousMode,
                                       get_configuration().validation.pcapyPollingIntervalMS)
        self._reader.setfilter('port ' + str(get_configuration().validation.pcapyMonitorPort))
        tpr.start_thread(thread_method=self._start)

    def stop(self):
        self._running = False

    # noinspection PyUnusedLocal
    def _pcapy_callback(self, packet_header, string):
        with self._lock:
            self._running_bytes_transferred += packet_header.getlen()

    def _reporting_thread(self):
        timeout = self._start_time_secs + (self._duration // 1000)
        sampling_interval = float(get_configuration().validation.pcapySamplingIntervalMS) / float(1000)

        while time.time() < timeout and self._running:
            time.sleep(sampling_interval)

            with self._lock:
                current_bytes_transferred = self._running_bytes_transferred

            print(str(current_bytes_transferred))

            ig.get_event_router().submit(EventTags.AnalyticsEventServerNetworkTrafficMeasuredBytes,
                                         _create_bytes_used_event(current_bytes_transferred))

    def _start(self):
        self._running = True
        ig.get_event_router().submit(EventTags.AnalyticsEventServerNetworkTrafficMeasuredBytes,
                                     _create_bytes_used_event(0))

        tpr.start_thread(thread_method=self._reporting_thread)

        timeout = self._start_time_secs + (self._duration // 1000)
        while self._running and time.time() < timeout:
            self._reader.dispatch(-1, self._pcapy_callback)  # noinspection PyMissingConstructor
