from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.qualitativefidelityassertion import QualitativeFidelityAssertion
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.dataproperty import DataProperty
from typing import List


# noinspection PyPep8Naming
class Fidelity(DataProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 fidelityRelationships: List[QualitativeFidelityAssertion] = None,
                 hidden: bool = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(hidden=hidden, truthConstraint=truthConstraint)
        self.fidelityRelationships = fidelityRelationships
