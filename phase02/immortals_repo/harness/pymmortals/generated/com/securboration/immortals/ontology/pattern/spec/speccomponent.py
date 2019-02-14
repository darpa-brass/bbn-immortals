from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.pattern.spec.paradigmcomponent import ParadigmComponent


# noinspection PyPep8Naming
class SpecComponent(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 abstractComponentLinkage: ParadigmComponent = None,
                 durableId: str = None,
                 spec: str = None):
        super().__init__()
        self.abstractComponentLinkage = abstractComponentLinkage
        self.durableId = durableId
        self.spec = spec
