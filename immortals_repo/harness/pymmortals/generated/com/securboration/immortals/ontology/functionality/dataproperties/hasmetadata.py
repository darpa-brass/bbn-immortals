from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataformat.dataformat import DataFormat
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.dataproperty import DataProperty
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType
from typing import List
from typing import Type


# noinspection PyPep8Naming
class HasMetadata(DataProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 hidden: bool = None,
                 metadataContent: List[Type[DataType]] = None,
                 metadataFormat: Type[DataFormat] = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(hidden=hidden, truthConstraint=truthConstraint)
        self.metadataContent = metadataContent
        self.metadataFormat = metadataFormat
