from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List


# noinspection PyPep8Naming
class Resource(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 resourceProperty: List[Property] = None):
        super().__init__()
        self.humanReadableDescription = humanReadableDescription
        self.resourceProperty = resourceProperty
