from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.qualitativefidelityassertion import QualitativeFidelityAssertion
from pymmortals.generated.com.securboration.immortals.ontology.image.fidelity.resolutionfidelity import ResolutionFidelity
from typing import List


# noinspection PyPep8Naming
class ImageSize1024x1024(ResolutionFidelity):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 fidelityRelationships: List[QualitativeFidelityAssertion] = None,
                 height: int = None,
                 hidden: bool = None,
                 truthConstraint: TruthConstraint = None,
                 width: int = None):
        super().__init__(fidelityRelationships=fidelityRelationships, height=height, hidden=hidden, truthConstraint=truthConstraint, width=width)
