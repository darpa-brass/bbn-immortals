from pymmortals.datatypes.serializable import Serializable
from typing import List


# noinspection PyPep8Naming
class JavaExecutionConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 args: List[str] = None,
                 jvmArgs: List[str] = None,
                 mainClassName: str = None):
        super().__init__()
        self.args = args
        self.jvmArgs = jvmArgs
        self.mainClassName = mainClassName
