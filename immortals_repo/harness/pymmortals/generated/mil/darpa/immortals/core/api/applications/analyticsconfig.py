from mil.darpa.immortals.core.analytics import AnalyticsVerbosity
from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.applications.analyticstarget import AnalyticsTarget


# noinspection PyPep8Naming
class AnalyticsConfig(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 port: int = None,
                 target: AnalyticsTarget = None,
                 url: str = None,
                 verbosity: AnalyticsVerbosity = None):
        super().__init__()
        self.port = port
        self.target = target
        self.url = url
        self.verbosity = verbosity
