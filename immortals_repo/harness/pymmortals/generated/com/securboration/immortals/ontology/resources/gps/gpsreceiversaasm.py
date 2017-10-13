from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.gpsreceiver import GpsReceiver
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.gpssatelliteconstellation import GpsSatelliteConstellation
from pymmortals.generated.com.securboration.immortals.ontology.resources.radiochannel import RadioChannel
from typing import List


# noinspection PyPep8Naming
class GpsReceiverSaasm(GpsReceiver):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 constellation: GpsSatelliteConstellation = None,
                 humanReadableDescription: str = None,
                 numChannels: int = None,
                 receivableSpectrum: List[RadioChannel] = None,
                 resourceProperty: List[Property] = None):
        super().__init__(constellation=constellation, humanReadableDescription=humanReadableDescription, numChannels=numChannels, receivableSpectrum=receivableSpectrum, resourceProperty=resourceProperty)
