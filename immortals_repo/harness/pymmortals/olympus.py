import json
import logging
from threading import Thread

import bottle
from bottle import Bottle
from pymmortals import threadprocessrouter as tpr
from tornado.httpserver import HTTPServer
from tornado.ioloop import IOLoop
from tornado.wsgi import WSGIContainer

from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.analyticsevent import AnalyticsEvent
from pymmortals.routing.eventrouter import EventRouter
from .datatypes.routing import EventTags

if get_configuration().visualization.enabled:
    from pymmortals import immortalsglobals as ig
    from .visualization.visualizationservice import VisualizationService

bottle.app.catchall = False


class ImmortalsBokehBottleServer(bottle.ServerAdapter):
    def __init__(self, host='127.0.0.1', port=8080, **config):
        super(ImmortalsBokehBottleServer, self).__init__(host, port, **config)
        if get_configuration().visualization.enabled:
            self._io_loop = IOLoop.instance()
            self._bokeh_server = VisualizationService(io_loop=self._io_loop, event_router=ig.get_event_router())
        else:
            self._io_loop_thread = None
            self._bokeh_server = None

    def run(self, handler):
        container = WSGIContainer(handler)
        server = HTTPServer(container)
        server.listen(port=self.port, address=self.host)

        if self._bokeh_server is None:
            IOLoop.instance().start()

        else:
            # tpr.start_thread(thread_method=self._io_loop.start)
            t = Thread(target=self._io_loop.start)
            t.setDaemon(True)
            t.start()
            self._bokeh_server.start()
            tpr.start_runtime_loop()

    def stop(self):
        self._io_loop_thread.stop()


class Olympus(Bottle):
    """
    :type event_router: EventRouter
    """

    def __init__(self, event_router):
        super(Olympus, self).__init__()

        self.event_router = event_router
        self._define_routes()
        self._host = get_configuration().testAdapter.url
        self._port = get_configuration().testAdapter.port
        self._analytics_event_listeners = set()
        self._validation_results_listeners = set()
        self._das_status_message_listeners = set()
        self.catchall = False

    def _define_routes(self):
        self.route(path='/analytics/receiveEvents', method='POST', callback=self._receive_analytics_event)
        self.route(path='/visualization/dasStatus', method='POST', callback=self._das_status_receive)

    def start(self):
        if get_configuration().debugMode:
            logging.getLogger('tornado.access').setLevel(logging.WARNING)
        else:
            logging.getLogger('tornado.access').setLevel(logging.INFO)

        ts = ImmortalsBokehBottleServer(host=self._host, port=self._port)
        bottle.run(app=self, server=ts, debug=False)

    def stop(self):
        # TODO: Fix this!
        # self.server.srv.stop()
        pass

    def route_add(self, path, method, callback):
        self.route(path=path, method=method, callback=callback)

    def add_redirect(self, path, method, redirect_url):
        def redirect():
            bottle.redirect(url=redirect_url)

        self.route(path=path, method=method, callback=redirect)

    """
    Analytics Event Routing
    """

    def _receive_analytics_event(self):
        # logging.warning(bottle.request.body.getvalue())
        val_j = bottle.request.json
        events = []

        try:
            events_list = json.loads(val_j)

            if type(events_list) is dict:
                events_list = [events_list]

            for d in events_list:
                events.append(AnalyticsEvent.from_dict(d))

        except Exception as e:
            raise Exception('The \'/analytics/receiveEvents/\' endpoint only accepts AnalyticsEvent Object(s)!' +
                            'Details: ' + str(e))

        self.event_router.submit(EventTags.AnalyticsEventReceived, data=events)

    """
    DAS Status Routing
    """

    def das_status_message_add_listener(self, listener):
        self._das_status_message_listeners.add(listener)

    def das_status_message_remove_listener(self, listener):
        self._das_status_message_listeners.remove(listener)

    def das_status_message_receive(self, msg):
        """
        :type msg: str
        """
        for l in self._das_status_message_listeners:
            l(msg)

    def _das_status_receive(self):
        data = bottle.request.json
        self.event_router.submit(EventTags.DASStatusMessage, data['statusMessage'])
