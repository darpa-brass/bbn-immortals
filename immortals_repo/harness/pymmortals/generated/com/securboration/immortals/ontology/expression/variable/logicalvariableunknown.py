from pymmortals.generated.com.securboration.immortals.ontology.expression.variable.logicalvariable import LogicalVariable


# noinspection PyPep8Naming
class LogicalVariableUnknown(LogicalVariable):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
