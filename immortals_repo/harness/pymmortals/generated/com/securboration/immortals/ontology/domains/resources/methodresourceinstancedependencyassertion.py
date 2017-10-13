from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource


# noinspection PyPep8Naming
class MethodResourceInstanceDependencyAssertion(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 consumerPointer: str = None,
                 dependencyAssurance: str = None,
                 originAssertion: 'MethodResourceInstanceDependencyAssertion' = None,
                 resourceConsumed: Resource = None):
        super().__init__()
        self.consumerPointer = consumerPointer
        self.dependencyAssurance = dependencyAssurance
        self.originAssertion = originAssertion
        self.resourceConsumed = resourceConsumed
