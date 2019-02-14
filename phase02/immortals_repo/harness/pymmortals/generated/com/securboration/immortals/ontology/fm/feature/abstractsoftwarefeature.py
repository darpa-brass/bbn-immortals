from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.fm.consumption.resourceconsumption import ResourceConsumption
from pymmortals.generated.com.securboration.immortals.ontology.fm.cutpoint.featureinjectioncutpoint import FeatureInjectionCutpoint
from pymmortals.generated.com.securboration.immortals.ontology.vp.featureprovides import FeatureProvides
from pymmortals.generated.com.securboration.immortals.ontology.vp.featurerequirement import FeatureRequirement
from typing import List


# noinspection PyPep8Naming
class AbstractSoftwareFeature(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 correspondingCodeCutpoint: FeatureInjectionCutpoint = None,
                 featureProvides: List[FeatureProvides] = None,
                 featureRequirement: List[FeatureRequirement] = None,
                 featureResourceConsumption: List[ResourceConsumption] = None):
        super().__init__()
        self.correspondingCodeCutpoint = correspondingCodeCutpoint
        self.featureProvides = featureProvides
        self.featureRequirement = featureRequirement
        self.featureResourceConsumption = featureResourceConsumption
