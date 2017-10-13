from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.structures import Structures
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.terrain import Terrain


# noinspection PyPep8Naming
class PhysicalOperatingEnvironment(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 structures: Structures = None,
                 terrain: Terrain = None):
        super().__init__()
        self.structures = structures
        self.terrain = terrain
