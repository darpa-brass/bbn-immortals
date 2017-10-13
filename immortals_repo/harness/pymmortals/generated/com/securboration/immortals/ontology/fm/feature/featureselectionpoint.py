from pymmortals.generated.com.securboration.immortals.ontology.fm.consumption.resourceconsumption import ResourceConsumption
from pymmortals.generated.com.securboration.immortals.ontology.fm.cutpoint.featureinjectioncutpoint import FeatureInjectionCutpoint
from pymmortals.generated.com.securboration.immortals.ontology.fm.feature.abstractsoftwarefeature import AbstractSoftwareFeature
from pymmortals.generated.com.securboration.immortals.ontology.fm.feature.featureselectioncriterion import FeatureSelectionCriterion
from pymmortals.generated.com.securboration.immortals.ontology.vp.featureprovides import FeatureProvides
from pymmortals.generated.com.securboration.immortals.ontology.vp.featurerequirement import FeatureRequirement
from typing import List


# noinspection PyPep8Naming
class FeatureSelectionPoint(AbstractSoftwareFeature):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 childSelectionCriterion: FeatureSelectionCriterion = None,
                 correspondingCodeCutpoint: FeatureInjectionCutpoint = None,
                 featureInGroup: List[AbstractSoftwareFeature] = None,
                 featureProvides: List[FeatureProvides] = None,
                 featureRequirement: List[FeatureRequirement] = None,
                 featureResourceConsumption: List[ResourceConsumption] = None):
        super().__init__(correspondingCodeCutpoint=correspondingCodeCutpoint, featureProvides=featureProvides, featureRequirement=featureRequirement, featureResourceConsumption=featureResourceConsumption)
        self.childSelectionCriterion = childSelectionCriterion
        self.featureInGroup = featureInGroup
