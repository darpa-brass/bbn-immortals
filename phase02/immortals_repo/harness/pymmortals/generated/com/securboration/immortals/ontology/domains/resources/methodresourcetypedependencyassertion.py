from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from typing import Type


# noinspection PyPep8Naming
class MethodResourceTypeDependencyAssertion(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 dependencyAssurance: str = None,
                 methodDesc: str = None,
                 methodName: str = None,
                 methodOwner: str = None,
                 originAssertion: 'MethodResourceTypeDependencyAssertion' = None,
                 resourceUtilized: Type[Resource] = None):
        super().__init__()
        self.dependencyAssurance = dependencyAssurance
        self.methodDesc = methodDesc
        self.methodName = methodName
        self.methodOwner = methodOwner
        self.originAssertion = originAssertion
        self.resourceUtilized = resourceUtilized
