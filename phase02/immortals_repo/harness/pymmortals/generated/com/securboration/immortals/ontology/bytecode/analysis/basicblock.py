from pymmortals.generated.com.securboration.immortals.ontology.core.humanreadable import HumanReadable
from pymmortals.generated.com.securboration.immortals.ontology.core.identifiable import Identifiable
from typing import List


# noinspection PyPep8Naming
class BasicBlock(HumanReadable, Identifiable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadable: str = None,
                 humanReadableDesc: str = None,
                 id: str = None,
                 instructions: List[int] = None,
                 predecessors: List['BasicBlock'] = None,
                 successors: List['BasicBlock'] = None):
        super().__init__()
        self.humanReadable = humanReadable
        self.humanReadableDesc = humanReadableDesc
        self.id = id
        self.instructions = instructions
        self.predecessors = predecessors
        self.successors = successors
