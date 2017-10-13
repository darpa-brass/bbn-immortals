from pymmortals.generated.com.securboration.immortals.ontology.ordering.orderingmechanism import OrderingMechanism
from typing import List
from typing import Type


# noinspection PyPep8Naming
class LogicalOrderingMechanism(OrderingMechanism):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 comesAfter: List[Type] = None,
                 comesBefore: List[Type] = None):
        super().__init__()
        self.comesAfter = comesAfter
        self.comesBefore = comesBefore
