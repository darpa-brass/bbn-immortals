from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.platformresource import PlatformResource
from typing import List


# noinspection PyPep8Naming
class Device(Resource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 resourceProperty: List[Property] = None,
                 resources: List[PlatformResource] = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.resources = resources
