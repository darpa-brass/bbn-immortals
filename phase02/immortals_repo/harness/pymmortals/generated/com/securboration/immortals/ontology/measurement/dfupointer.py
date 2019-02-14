from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionalaspect import FunctionalAspect
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionality import Functionality
from pymmortals.generated.com.securboration.immortals.ontology.measurement.codeunitpointer import CodeUnitPointer
from typing import Type


# noinspection PyPep8Naming
class DfuPointer(CodeUnitPointer):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 className: str = None,
                 methodName: str = None,
                 pointerString: str = None,
                 relevantFunctionalAspect: Type[FunctionalAspect] = None,
                 relevantFunctionality: Type[Functionality] = None):
        super().__init__(className=className, methodName=methodName, pointerString=pointerString)
        self.relevantFunctionalAspect = relevantFunctionalAspect
        self.relevantFunctionality = relevantFunctionality
