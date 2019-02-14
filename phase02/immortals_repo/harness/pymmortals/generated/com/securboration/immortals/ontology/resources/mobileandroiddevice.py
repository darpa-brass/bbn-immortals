from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.mobiledevice import MobileDevice
from pymmortals.generated.com.securboration.immortals.ontology.resources.platformresource import PlatformResource
from typing import List


# noinspection PyPep8Naming
class MobileAndroidDevice(MobileDevice):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 resourceProperty: List[Property] = None,
                 resources: List[PlatformResource] = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty, resources=resources)
