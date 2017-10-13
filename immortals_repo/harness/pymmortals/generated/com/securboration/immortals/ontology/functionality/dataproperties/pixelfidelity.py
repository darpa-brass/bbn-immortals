from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.fidelity import Fidelity
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.qualitativefidelityassertion import QualitativeFidelityAssertion
from typing import List


# noinspection PyPep8Naming
class PixelFidelity(Fidelity):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 fidelityRelationships: List[QualitativeFidelityAssertion] = None,
                 hidden: bool = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(fidelityRelationships=fidelityRelationships, hidden=hidden, truthConstraint=truthConstraint)
