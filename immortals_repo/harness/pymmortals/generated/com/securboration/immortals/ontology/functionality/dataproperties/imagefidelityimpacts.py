from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.imagefidelityimpact import ImageFidelityImpact
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.dataproperty import DataProperty
from typing import List


# noinspection PyPep8Naming
class ImageFidelityImpacts(DataProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 hidden: bool = None,
                 imageFidelityImpacts: List[ImageFidelityImpact] = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(hidden=hidden, truthConstraint=truthConstraint)
        self.imageFidelityImpacts = imageFidelityImpacts
