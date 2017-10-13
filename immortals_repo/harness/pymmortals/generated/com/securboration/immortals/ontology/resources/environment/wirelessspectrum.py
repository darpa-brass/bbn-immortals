from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class WirelessSpectrum(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 maxFrequency: float = None,
                 minFrequency: float = None):
        super().__init__()
        self.maxFrequency = maxFrequency
        self.minFrequency = minFrequency
