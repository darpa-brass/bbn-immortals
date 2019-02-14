from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.spectrumkeying import SpectrumKeying
from pymmortals.generated.com.securboration.immortals.ontology.resources.radiochannel import RadioChannel
from typing import List
from typing import Type


# noinspection PyPep8Naming
class L5(RadioChannel):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 centerFrequencyHertz: float = None,
                 channelName: str = None,
                 channelProperties: List[Type[Property]] = None,
                 humanReadableDescription: str = None,
                 maxFrequencyHertz: float = None,
                 minFrequencyHertz: float = None,
                 modulationStrategy: SpectrumKeying = None,
                 resourceProperty: List[Property] = None):
        super().__init__(centerFrequencyHertz=centerFrequencyHertz, channelName=channelName, channelProperties=channelProperties, humanReadableDescription=humanReadableDescription, maxFrequencyHertz=maxFrequencyHertz, minFrequencyHertz=minFrequencyHertz, modulationStrategy=modulationStrategy, resourceProperty=resourceProperty)
