from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType


# noinspection PyPep8Naming
class Location(DataType):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 altitudeWGS84: float = None,
                 latitude: float = None,
                 longitude: float = None):
        super().__init__()
        self.altitudeWGS84 = altitudeWGS84
        self.latitude = latitude
        self.longitude = longitude
