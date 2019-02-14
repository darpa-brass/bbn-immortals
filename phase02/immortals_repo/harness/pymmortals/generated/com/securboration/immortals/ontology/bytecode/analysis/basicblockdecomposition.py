from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.bytecode.analysis.basicblock import BasicBlock
from typing import List


# noinspection PyPep8Naming
class BasicBlockDecomposition(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 nodes: List[BasicBlock] = None,
                 root: BasicBlock = None):
        super().__init__()
        self.nodes = nodes
        self.root = root
