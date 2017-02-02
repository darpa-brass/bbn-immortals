# noinspection PyPep8Naming
import json
import pcapy
import time
from threading import Lock

import bottle

from .. import immortalsglobals as ig
from .. import threadprocessrouter as tpr
from ..data.base.root_configuration import demo_mode
from ..data.base.scenarioapiconfiguration import ScenarioConductorConfiguration
from ..ll_api.data import AnalyticsEvent

if demo_mode:
    from ..visualization.objects import CartesianChart

_interface = 'lo0'
_port = 8088
_timeout_ms = 1000

_event_ticker_lock = Lock()
_event_ticker = 0

snapshot_length = 4

LOCAL_VALIDATORS = frozenset([
    'bandwidth-maximum-validator'
])


def _get_validator_by_identifier(identifier):
    if identifier == 'bandwidth-maximum-validator':
        return BandwidthMonitor


class MonitorManager:
    """
    :type _monitors: MonitorInterface
    """

    def __init__(self, scenario_configuration, validator_identifiers, listeners):
        self._scenario_configuration = scenario_configuration
        self._validator_identifiers = validator_identifiers
        self._listeners = listeners
        self._monitors = []

        self._monitors.append(NetworkAnalyticsMonitor(scenario_configuration, self._listener))

        for m_id in validator_identifiers:
            m = _get_validator_by_identifier(m_id)(scenario_configuration, self._listener)
            self._monitors.append(m)

    def _listener(self, event):
        for l in self._listeners:
            l(event)

    def start(self):
        for m in self._monitors:
            m.start()

    def stop(self):
        for m in self._monitors:
            m.stop()


def _create_bytes_used_event(bytes_used, event_time_ms=None):
    global _event_ticker, _event_ticker_lock

    with _event_ticker_lock:
        if event_time_ms is None:
            event_time_ms = time.time() * 1000

        event = AnalyticsEvent(
            type='combinedServerTrafficBytes',
            eventSource='global',
            eventTime=event_time_ms,
            eventRemoteSource='global',
            dataType='java.lang.Long',
            eventId=_event_ticker,
            data=str(bytes_used)
        )
        _event_ticker += 1
        return event


class MonitorInterface:
    def start(self):
        raise NotImplementedError

    def stop(self):
        raise NotImplementedError

    def __init__(self, scenario_configuration, listener):
        raise NotImplementedError


# noinspection PyMissingConstructor
class BandwidthMonitor(MonitorInterface):
    """
    :type reporting_interval_ms: long
    :type _figure: CartesianChart
    """

    def __init__(self, scenario_configuration, listener):
        """
        :type scenario_configuration: ScenarioConductorConfiguration
        """
        self.reporting_interval_ms = 1000
        self._start_time_secs = None
        self._last_polling_time_secs = None
        self._running_bytes_transferred = 0
        self._reader = None
        self._running = False
        self._listener = listener
        self.maximum_bandwidth_kbytes_per_second = scenario_configuration.server.bandwidth / 8
        self._lock = Lock()

        self._time_list = []
        self._value_list = []

        self._three_second_window_bytes = []
        self._three_second_window_times = []

        for i in range(0, snapshot_length):
            self._three_second_window_bytes.append(0)
            self._three_second_window_times.append(0)

    def start(self):
        self._start_time_secs = time.time()
        self._reader = pcapy.open_live(ig.configuration.validation.pcapyMonitorInterface, 4096, False, self.reporting_interval_ms)
        self._reader.setfilter('port ' + str(_port))
        self._running = True

        tpr.start_thread(thread_method=self._start)

    def stop(self):
        self._running = False

    def _pcapy_callback(self, packet_header, string):
        with self._lock:
            self._running_bytes_transferred += packet_header.getlen()

    def _start(self):
        self._running = True
        self._listener(_create_bytes_used_event(0))

        def l():

            window_counter = 0
            last_bytes_transferred = 0

            while self._running:
                time.sleep(1)

                with self._lock:
                    now = time.time()
                    current_bytes_transferred = self._running_bytes_transferred

                delta_bytes = current_bytes_transferred - last_bytes_transferred

                if self._last_polling_time_secs is None:
                    self._last_polling_time_secs = now

                delta_time = now - self._last_polling_time_secs
                self._last_polling_time_secs = now

                if window_counter == snapshot_length:
                    window_counter = 0

                self._three_second_window_bytes[window_counter] = delta_bytes
                self._three_second_window_times[window_counter] = delta_time

                average_time = sum(self._three_second_window_times) / snapshot_length
                average_bytes = sum(self._three_second_window_bytes) / snapshot_length

                # TODO: Add to validator calculation
                self._listener(_create_bytes_used_event(self._running_bytes_transferred))

                # TODO: Cleanup
                # delta = self._last_polling_time_secs - self._start_time_secs
                self._time_list.append(average_time)
                if average_time > 0:
                    self._time_list.append(average_time)
                    self._value_list.append((average_bytes / 1000) / average_time)
                    if demo_mode:
                        ig.get_olympus().demo.update_bandwidth(now - self._start_time_secs, (average_bytes / 1000))

                print('Time Delta: ' + str(delta_time))
                print('Bytes Delta: ' + str(delta_bytes))
                print('Total Time: ' + str(now - self._start_time_secs))
                print('Average Bytes: ' + str(average_bytes))
                print('Average Time: ' + str(average_time))
                print('three second window time: ' + str(self._three_second_window_times))
                print('three second window bytes: ' + str(self._three_second_window_bytes))

                last_bytes_transferred = current_bytes_transferred
                window_counter += 1

        t = tpr.start_thread(thread_method=l)

        while self._running:
            self._reader.dispatch(-1, self._pcapy_callback)  # noinspection PyMissingConstructor


class NetworkAnalyticsMonitor(MonitorInterface):
    def __init__(self, scenario_configuration, listener):
        self._listener = listener
        self._is_running = False
        self._lock = Lock()
        olympus = ig.get_olympus()
        olympus.add_call(path='/analytics/receiveEvents', method='POST', callback=self._process_received_data)
        pass

    def _process_received_data(self):
        val_j = bottle.request.json
        events = []

        try:
            events_list = json.loads(val_j)

            if type(events_list) is dict:
                events_list = [events_list]

            for d in events_list:
                events.append(AnalyticsEvent.from_dict(d))

        except:
            raise Exception('The \'/analytics/receiveEvents/\' endpoint only accepts AnalyticsEvent Object(s)!')

        for e in events:
            self._listener(e)

    def start(self):
        pass

    def stop(self):
        pass

    def get_figures(self):
        return []
