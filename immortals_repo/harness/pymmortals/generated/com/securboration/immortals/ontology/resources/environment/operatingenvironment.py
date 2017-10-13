from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.gpsenvironment import GpsEnvironment
from pymmortals.generated.com.securboration.immortals.ontology.resources.network.networktopology import NetworkTopology


# noinspection PyPep8Naming
class OperatingEnvironment(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 gps: GpsEnvironment = None,
                 network: NetworkTopology = None):
        super().__init__()
        self.gps = gps
        self.network = network
