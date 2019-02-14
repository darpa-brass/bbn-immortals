from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.imagefidelitytype import ImageFidelityType
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.impacttype import ImpactType
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.dataproperty import DataProperty
from typing import List


# noinspection PyPep8Naming
class ImageFidelityImpact(DataProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 fidelityDimensions: List[ImageFidelityType] = None,
                 fidelityImpacts: List[ImpactType] = None,
                 hidden: bool = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(hidden=hidden, truthConstraint=truthConstraint)
        self.fidelityDimensions = fidelityDimensions
        self.fidelityImpacts = fidelityImpacts
