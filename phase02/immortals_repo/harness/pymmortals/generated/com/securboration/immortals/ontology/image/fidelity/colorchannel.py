from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.image.fidelity.colortype import ColorType


# noinspection PyPep8Naming
class ColorChannel(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 bitDepth: int = None,
                 channelColor: ColorType = None):
        super().__init__()
        self.bitDepth = bitDepth
        self.channelColor = channelColor
