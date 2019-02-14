from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.structures import Structures
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.terrain import Terrain


# noinspection PyPep8Naming
class GpsEnvironment(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 numberOfVisibleSatellites: int = None,
                 structures: Structures = None,
                 terrainModel: Terrain = None):
        super().__init__()
        self.numberOfVisibleSatellites = numberOfVisibleSatellites
        self.structures = structures
        self.terrainModel = terrainModel
