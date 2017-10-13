from bokeh.application import Application
from bokeh.application.handlers import FunctionHandler


class AbstractBokehDashboard:
    def __init__(self):
        self._application = None

    def modify_doc(self, doc):
        raise NotImplementedError

    def get_application(self):
        self._application = Application(FunctionHandler(self.modify_doc))
        return self._application
