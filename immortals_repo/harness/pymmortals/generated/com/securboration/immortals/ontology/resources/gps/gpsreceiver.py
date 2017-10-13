from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.gpssatelliteconstellation import GpsSatelliteConstellation
from pymmortals.generated.com.securboration.immortals.ontology.resources.platformresource import PlatformResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.radiochannel import RadioChannel
from typing import List


# noinspection PyPep8Naming
class GpsReceiver(PlatformResource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 constellation: GpsSatelliteConstellation = None,
                 humanReadableDescription: str = None,
                 numChannels: int = None,
                 receivableSpectrum: List[RadioChannel] = None,
                 resourceProperty: List[Property] = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.constellation = constellation
        self.numChannels = numChannels
        self.receivableSpectrum = receivableSpectrum
