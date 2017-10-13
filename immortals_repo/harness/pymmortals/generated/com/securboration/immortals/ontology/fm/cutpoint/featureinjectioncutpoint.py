from pymmortals.generated.com.securboration.immortals.ontology.core.humanreadable import HumanReadable
from pymmortals.generated.com.securboration.immortals.ontology.core.uniquelyidentifiable import UniquelyIdentifiable


# noinspection PyPep8Naming
class FeatureInjectionCutpoint(HumanReadable, UniquelyIdentifiable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDesc: str = None,
                 uuid: str = None):
        super().__init__()
        self.humanReadableDesc = humanReadableDesc
        self.uuid = uuid
