from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.gpssatellite import GpsSatellite
from typing import List


# noinspection PyPep8Naming
class GpsSatelliteConstellation(Resource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 constellationName: str = None,
                 humanReadableDescription: str = None,
                 resourceProperty: List[Property] = None,
                 satellites: List[GpsSatellite] = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.constellationName = constellationName
        self.satellites = satellites
