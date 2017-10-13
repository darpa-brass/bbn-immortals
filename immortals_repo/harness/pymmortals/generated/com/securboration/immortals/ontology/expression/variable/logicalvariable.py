from pymmortals.generated.com.securboration.immortals.ontology.expression.variable.expressionvariable import ExpressionVariable


# noinspection PyPep8Naming
class LogicalVariable(ExpressionVariable):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
