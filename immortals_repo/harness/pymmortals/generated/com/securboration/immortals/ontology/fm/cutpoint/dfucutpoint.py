from pymmortals.generated.com.securboration.immortals.ontology.dfu.instance.dfuinstance import DfuInstance
from pymmortals.generated.com.securboration.immortals.ontology.fm.cutpoint.featureinjectioncutpoint import FeatureInjectionCutpoint


# noinspection PyPep8Naming
class DfuCutpoint(FeatureInjectionCutpoint):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 dfu: DfuInstance = None,
                 humanReadableDesc: str = None,
                 uuid: str = None):
        super().__init__(humanReadableDesc=humanReadableDesc, uuid=uuid)
        self.dfu = dfu
