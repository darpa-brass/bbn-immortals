import json

import bottle
import logging

from .data.base.root_configuration import demo_mode, debug_mode
from .ll_api.data import AnalyticsEvent

if demo_mode:
    from .visualization.objects import CartesianChart, DasStatusVisualization, DemoDashboard
    from bokeh.client import push_session
    from bokeh.document import Document
    from bokeh.layouts import row
    from bokeh.plotting import curdoc


class BokehServ:
    """
    :type _diagrams: dict[str, CartesianChart]
    :type _doc: Document
    """

    def __init__(self):
        if demo_mode:
            self._doc = curdoc()
            self._diagrams = {}
            self._session = push_session(self._doc, session_id='5EFF4189')
            self.demo_dashboard = DemoDashboard()
            self._doc.add_root(self.demo_dashboard.get_bokeh_object())

    def add_diagrams(self, diagrams):
        """
        :type: diagrams: list[AbstractBokehProvider] or AbstractBokehProvider
        :return: 
        """
        if demo_mode:
            diagrams = diagrams if type(diagrams) is list else [diagrams]
            bokeh_objects = []
            for diagram in diagrams:
                self._diagrams[diagram.title] = diagram
                bokeh_objects.append(diagram.get_bokeh_diagram())

            self._doc.add_root(row(bokeh_objects))

    def update_diagram(self, title, x, y):
        if demo_mode:
            self._diagrams[title].update(x, y)

    def start(self):
        if demo_mode:
            self._session.show()

    def bk_app(self):
        if demo_mode:
            return self._bokeh_server.show('/')


class Olympus(bottle.Bottle):
    """
    :type das_status_visualization: DasStatusVisualization
    :type demo: DemoDashboard
    """

    def __init__(self, host, port):
        super(Olympus, self).__init__()
        self._define_routes()
        self._host = host
        self._port = port
        self._analytics_event_listeners = set()

        if demo_mode:
            self._bokeh_server = BokehServ()
            self.demo = self._bokeh_server.demo_dashboard

    def initialize_demo_visualization(self, scenario_configuration):
        if demo_mode:
            self.demo = self._bokeh_server.initialize_demo(scenario_configuration=scenario_configuration)

    def _define_routes(self):
        if demo_mode:
            self.route(path='/visualization/createChart', method='POST', callback=self.create_chart)
            self.route(path='/visualization/updateChart', method='POST', callback=self.update_chart)
            self.route(path='/visualization/dasStatus', method='POST', callback=self._das_status)
            self.route(path='/dashboard', method='GET', callback=self._dashboard)

    def _dashboard(self):
        if demo_mode:
            bottle.redirect('http://127.0.0.1:5006/?bokeh-session-id=5EFF4189');

    def analytics_events_listener_add(self, listener):
        self._analytics_event_listeners.add(listener)

    def analytics_event_listener_remove(self, listener):
        self._analytics_event_listeners.remove(listener)

    def _das_status(self):
        if demo_mode:
            data = bottle.request.json
            self.demo.adapting(data['statusMessage'])

    def print_event(self):
        data = bottle.request.body.read()
        print("DATA: " + data)

    def start(self):
        if demo_mode:
            self._bokeh_server.start()

        if debug_mode:
            logging.getLogger('tornado.access').setLevel(logging.WARNING)
        else:
            logging.getLogger('tornado.access').setLevel(logging.INFO)
        bottle.run(app=self, server='tornado', host=self._host, port=self._port, debug=False)

    def stop(self):
        self.server.srv.stop()

    def create_chart(self):
        if demo_mode:
            val_j = bottle.request.json

            diagram = CartesianChart.from_dict(val_j)
            self._bokeh_server.add_diagrams(diagram)
            print('CREATE CHART')

    def update_chart(self):
        if demo_mode:
            data = bottle.request.json
            self._bokeh_server.update_diagram(data['title'], data['x'], data['y'])
            print('UPDATE CHART')

    def add_call(self, path, method, callback):
        self.route(path=path, method=method, callback=callback)

    def analytics_receive_event(self):
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

        for l in self._analytics_event_listeners:
            l(events)

    def add_diagrams(self, diagrams):
        if demo_mode:
            self._bokeh_server.add_diagrams(diagrams)
