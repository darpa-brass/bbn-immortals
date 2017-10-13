from pymmortals.generated.com.securboration.immortals.ontology.expression.expressionnodeunary import ExpressionNodeUnary


# noinspection PyPep8Naming
class ExpressionNodeValue(ExpressionNodeUnary):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
