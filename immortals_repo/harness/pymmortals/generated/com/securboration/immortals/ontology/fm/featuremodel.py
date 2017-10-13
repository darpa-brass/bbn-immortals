from pymmortals.generated.com.securboration.immortals.ontology.core.humanreadable import HumanReadable
from pymmortals.generated.com.securboration.immortals.ontology.fm.feature.featureselectionpoint import FeatureSelectionPoint
from typing import List


# noinspection PyPep8Naming
class FeatureModel(HumanReadable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDesc: str = None,
                 variationPoints: List[FeatureSelectionPoint] = None):
        super().__init__()
        self.humanReadableDesc = humanReadableDesc
        self.variationPoints = variationPoints
