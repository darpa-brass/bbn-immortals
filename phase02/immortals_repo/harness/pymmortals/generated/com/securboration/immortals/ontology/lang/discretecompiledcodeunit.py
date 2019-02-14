from pymmortals.generated.com.securboration.immortals.ontology.lang.compiledcodeunit import CompiledCodeUnit
from pymmortals.generated.com.securboration.immortals.ontology.lang.sourcefile import SourceFile


# noinspection PyPep8Naming
class DiscreteCompiledCodeUnit(CompiledCodeUnit):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 compiledForm: bytes = None,
                 source: SourceFile = None):
        super().__init__()
        self.compiledForm = compiledForm
        self.source = source
