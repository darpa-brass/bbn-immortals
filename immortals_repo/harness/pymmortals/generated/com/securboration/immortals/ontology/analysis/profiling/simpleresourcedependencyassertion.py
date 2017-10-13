from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.measurement.codeunitpointer import CodeUnitPointer
from typing import Type


# noinspection PyPep8Naming
class SimpleResourceDependencyAssertion(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 codeUnit: CodeUnitPointer = None,
                 dependency: Type[Resource] = None):
        super().__init__()
        self.codeUnit = codeUnit
        self.dependency = dependency
