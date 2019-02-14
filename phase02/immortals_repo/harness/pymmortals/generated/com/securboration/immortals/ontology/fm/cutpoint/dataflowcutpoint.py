from pymmortals.generated.com.securboration.immortals.ontology.fm.cutpoint.featureinjectioncutpoint import FeatureInjectionCutpoint


# noinspection PyPep8Naming
class DataflowCutpoint(FeatureInjectionCutpoint):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 dataflowId: str = None,
                 humanReadableDesc: str = None,
                 injectedCodeTemplate: str = None,
                 uuid: str = None):
        super().__init__(humanReadableDesc=humanReadableDesc, injectedCodeTemplate=injectedCodeTemplate, uuid=uuid)
        self.dataflowId = dataflowId
