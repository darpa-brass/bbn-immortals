from bokeh.application import Application
from bokeh.client.session import show_session
from bokeh.io import logger
from bokeh.server.server import Server as BokehServer

from pymmortals.datatypes.root_configuration import get_configuration, VisualizationConfiguration
from pymmortals.routing.eventrouter import EventRouter
from .calculatedbandwidthdashboard import CalculatedBandwidthDashboard
from .emulatortimeanalysisdashboard import EmulatorTimeAnalysisDashboard
from .immortalsdashboard import ImmortalsDashboard, ImmortalsDashboardManager


class VisualizationService:
    """
    :type _config: VisualizationConfiguration
    :type _bokeh_apps: dict
    :type _event_router: EventRouter
    :type _immortals_dashboard: ImmortalsDashboard
    :type _emulator_timing_dashboard: EmulatorTimeAnalysisDashboard
    :type _calculated_bandwidth_dashboard: CalculatedBandwidthDashboard
    """

    def __init__(self, io_loop, event_router):
        """
        :type io_loop: tornado.ioloop.IOLoop
        :type event_router: EventRouter
        """

        self._config = get_configuration().visualization
        self._bokeh_apps = {'/': Application()}
        self._event_router = event_router
        self._immortals_dashboards = dict()

        if self._config.enableImmortalsDashboard:
            self._immortals_dashboard = ImmortalsDashboardManager(event_router=event_router)
            self._bokeh_apps['/immortals'] = self._immortals_dashboard.get_application()

        if self._config.enableTimingDashboard:
            self._emulator_timing_dashboard = EmulatorTimeAnalysisDashboard()
            self._bokeh_apps['/emulatorTiming'] = self._emulator_timing_dashboard.get_application()

        if self._config.enableBandwidthCalculationsStaticDashboard:
            self._calculated_bandwidth_dashboard = CalculatedBandwidthDashboard()
            self._bokeh_apps['/calculatedBandwidth'] = self._calculated_bandwidth_dashboard.get_application()

        self._bokeh_server = BokehServer(applications=self._bokeh_apps, io_loop=io_loop)

    def start(self):
        logger.info('Bokeh Server Starting')
        self._bokeh_server.start()

        vc = get_configuration().visualization

        if vc.enableBandwidthCalculationsStaticDashboard:
            show_session(session_id='5EFF4189', app_path='/calculatedBandwidth')

        if vc.enableTimingDashboard:
            show_session(session_id='5EFF4189', app_path='/emulatorTiming')

        logger.info('Bokeh Server Started')
