from pymmortals.generated.com.securboration.immortals.ontology.lang.compiledcodeunit import CompiledCodeUnit
from pymmortals.generated.com.securboration.immortals.ontology.lang.sourcefile import SourceFile
from typing import List


# noinspection PyPep8Naming
class AggregateCompiledCodeUnit(CompiledCodeUnit):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 compiledForm: bytes = None,
                 sourceFiles: List[SourceFile] = None):
        super().__init__()
        self.compiledForm = compiledForm
        self.sourceFiles = sourceFiles
