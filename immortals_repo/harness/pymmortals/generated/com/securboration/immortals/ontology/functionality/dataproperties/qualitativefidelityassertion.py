from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.constraint.binarycomparisonoperatortype import BinaryComparisonOperatorType
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.fidelity import Fidelity
from typing import List
from typing import Type


# noinspection PyPep8Naming
class QualitativeFidelityAssertion(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 objectOfAssertion: List[Type[Fidelity]] = None,
                 operator: BinaryComparisonOperatorType = None,
                 subjectOfAssertion: Type[Fidelity] = None):
        super().__init__()
        self.objectOfAssertion = objectOfAssertion
        self.operator = operator
        self.subjectOfAssertion = subjectOfAssertion
