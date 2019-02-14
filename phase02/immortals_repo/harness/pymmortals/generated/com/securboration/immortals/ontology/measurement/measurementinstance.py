from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property


# noinspection PyPep8Naming
class MeasurementInstance(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 measuredValue: Property = None,
                 qualifier: str = None):
        super().__init__()
        self.measuredValue = measuredValue
        self.qualifier = qualifier
