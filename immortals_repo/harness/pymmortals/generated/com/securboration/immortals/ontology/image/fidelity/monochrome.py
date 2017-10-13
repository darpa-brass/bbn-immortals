from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.qualitativefidelityassertion import QualitativeFidelityAssertion
from pymmortals.generated.com.securboration.immortals.ontology.image.fidelity.colorchannel import ColorChannel
from pymmortals.generated.com.securboration.immortals.ontology.image.fidelity.colorfidelity import ColorFidelity
from typing import List


# noinspection PyPep8Naming
class Monochrome(ColorFidelity):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 channels: List[ColorChannel] = None,
                 fidelityRelationships: List[QualitativeFidelityAssertion] = None,
                 hidden: bool = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(channels=channels, fidelityRelationships=fidelityRelationships, hidden=hidden, truthConstraint=truthConstraint)
