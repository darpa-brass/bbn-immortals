from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource


# noinspection PyPep8Naming
class DataflowNode(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 contextTemplate: Resource = None,
                 resourceTemplate: Resource = None):
        super().__init__()
        self.contextTemplate = contextTemplate
        self.resourceTemplate = resourceTemplate
