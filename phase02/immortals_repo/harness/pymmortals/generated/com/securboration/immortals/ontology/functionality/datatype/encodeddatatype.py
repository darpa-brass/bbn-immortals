from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.encoding import Encoding


# noinspection PyPep8Naming
class EncodedDataType(DataType):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 encoding: Encoding = None):
        super().__init__()
        self.encoding = encoding
