from pymmortals.generated.com.securboration.immortals.ontology.bytecode.analysis.instruction import Instruction
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.invocationtype import InvocationType


# noinspection PyPep8Naming
class MethodCall(Instruction):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 calledMethodDesc: str = None,
                 calledMethodName: str = None,
                 invocationType: InvocationType = None,
                 order: int = None,
                 owner: str = None,
                 ownerAssurance: bool = None):
        super().__init__()
        self.calledMethodDesc = calledMethodDesc
        self.calledMethodName = calledMethodName
        self.invocationType = invocationType
        self.order = order
        self.owner = owner
        self.ownerAssurance = ownerAssurance
