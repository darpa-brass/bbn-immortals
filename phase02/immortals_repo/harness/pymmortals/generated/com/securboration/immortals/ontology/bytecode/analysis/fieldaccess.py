from pymmortals.generated.com.securboration.immortals.ontology.bytecode.analysis.instruction import Instruction


# noinspection PyPep8Naming
class FieldAccess(Instruction):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 fieldDesc: str = None,
                 fieldName: str = None,
                 fieldOwnerHash: str = None):
        super().__init__()
        self.fieldDesc = fieldDesc
        self.fieldName = fieldName
        self.fieldOwnerHash = fieldOwnerHash
