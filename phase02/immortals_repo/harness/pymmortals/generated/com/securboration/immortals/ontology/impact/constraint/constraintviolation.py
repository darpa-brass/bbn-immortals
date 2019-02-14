from pymmortals.generated.com.securboration.immortals.ontology.impact.constraint.violationimpact import ViolationImpact


# noinspection PyPep8Naming
class ConstraintViolation(ViolationImpact):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
