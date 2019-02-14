from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.dfu.annotation.dfuannotation import DfuAnnotation
from typing import List


# noinspection PyPep8Naming
class DfuAnnotations(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 dfus: List[DfuAnnotation] = None):
        super().__init__()
        self.dfus = dfus
