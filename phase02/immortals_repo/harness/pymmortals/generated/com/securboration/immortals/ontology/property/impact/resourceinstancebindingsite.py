from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.assertionbindingsite import AssertionBindingSite


# noinspection PyPep8Naming
class ResourceInstanceBindingSite(AssertionBindingSite):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 resourceInstance: Resource = None):
        super().__init__(humanReadableDescription=humanReadableDescription)
        self.resourceInstance = resourceInstance
