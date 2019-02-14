from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List


# noinspection PyPep8Naming
class GpsSatellite(Resource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 hasFineGrainedEncryptedLocation: bool = None,
                 humanReadableDescription: str = None,
                 resourceProperty: List[Property] = None,
                 satelliteId: str = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.hasFineGrainedEncryptedLocation = hasFineGrainedEncryptedLocation
        self.satelliteId = satelliteId
