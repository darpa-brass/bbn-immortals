from pymmortals.generated.com.securboration.immortals.ontology.expression.booleanexpressionnode import BooleanExpressionNode


# noinspection PyPep8Naming
class ExpressionNodeNumerical(BooleanExpressionNode):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
