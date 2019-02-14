from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.fidelityresourcerelationship import FidelityResourceRelationship
from typing import List


# noinspection PyPep8Naming
class FidelityResourceRelationships(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 relationships: List[FidelityResourceRelationship] = None):
        super().__init__()
        self.relationships = relationships
