from pymmortals.generated.com.securboration.immortals.ontology.analysis.profiling.properties.measuredproperty import MeasuredProperty
from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint


# noinspection PyPep8Naming
class DynamicInstructionCount(MeasuredProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 numberOfDynamicInstructionsExecuted: int = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(truthConstraint=truthConstraint)
        self.numberOfDynamicInstructionsExecuted = numberOfDynamicInstructionsExecuted
