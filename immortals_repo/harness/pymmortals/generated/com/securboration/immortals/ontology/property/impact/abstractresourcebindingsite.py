from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.assertionbindingsite import AssertionBindingSite
from typing import Type


# noinspection PyPep8Naming
class AbstractResourceBindingSite(AssertionBindingSite):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 resourceType: Type[Resource] = None):
        super().__init__(humanReadableDescription=humanReadableDescription)
        self.resourceType = resourceType
