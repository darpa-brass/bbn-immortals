from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.imagefidelity import ImageFidelity
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.qualitativefidelityassertion import QualitativeFidelityAssertion
from typing import List


# noinspection PyPep8Naming
class ResolutionFidelity(ImageFidelity):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 fidelityRelationships: List[QualitativeFidelityAssertion] = None,
                 height: int = None,
                 hidden: bool = None,
                 truthConstraint: TruthConstraint = None,
                 width: int = None):
        super().__init__(fidelityRelationships=fidelityRelationships, hidden=hidden, truthConstraint=truthConstraint)
        self.height = height
        self.width = width
