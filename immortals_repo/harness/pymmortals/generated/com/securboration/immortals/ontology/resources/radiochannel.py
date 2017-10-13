from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.communicationchannel import CommunicationChannel
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.spectrumkeying import SpectrumKeying
from typing import List
from typing import Type


# noinspection PyPep8Naming
class RadioChannel(CommunicationChannel):
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
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.centerFrequencyHertz = centerFrequencyHertz
        self.channelName = channelName
        self.channelProperties = channelProperties
        self.maxFrequencyHertz = maxFrequencyHertz
        self.minFrequencyHertz = minFrequencyHertz
        self.modulationStrategy = modulationStrategy
