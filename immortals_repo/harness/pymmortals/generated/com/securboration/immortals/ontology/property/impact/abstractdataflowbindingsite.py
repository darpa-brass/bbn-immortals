from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.assertionbindingsite import AssertionBindingSite
from typing import Type


# noinspection PyPep8Naming
class AbstractDataflowBindingSite(AssertionBindingSite):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 dest: Type[Resource] = None,
                 humanReadableDescription: str = None,
                 src: Type[Resource] = None):
        super().__init__(humanReadableDescription=humanReadableDescription)
        self.dest = dest
        self.src = src
