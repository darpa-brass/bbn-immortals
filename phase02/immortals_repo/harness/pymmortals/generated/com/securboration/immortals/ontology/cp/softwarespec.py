from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.ordering.explicitnumericorderingmechanism import ExplicitNumericOrderingMechanism


# noinspection PyPep8Naming
class SoftwareSpec(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 precedenceOfSpec: ExplicitNumericOrderingMechanism = None):
        super().__init__()
        self.precedenceOfSpec = precedenceOfSpec
