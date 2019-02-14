from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.pattern.spec.paradigmcomponent import ParadigmComponent
from typing import List


# noinspection PyPep8Naming
class AbstractUsageParadigm(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 component: List[ParadigmComponent] = None,
                 durableId: str = None):
        super().__init__()
        self.component = component
        self.durableId = durableId
