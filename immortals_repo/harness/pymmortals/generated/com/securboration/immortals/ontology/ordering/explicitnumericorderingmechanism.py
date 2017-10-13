from pymmortals.generated.com.securboration.immortals.ontology.ordering.orderingmechanism import OrderingMechanism


# noinspection PyPep8Naming
class ExplicitNumericOrderingMechanism(OrderingMechanism):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 precedence: int = None):
        super().__init__()
        self.precedence = precedence
