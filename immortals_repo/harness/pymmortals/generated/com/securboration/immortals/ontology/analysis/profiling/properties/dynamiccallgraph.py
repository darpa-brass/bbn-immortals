from pymmortals.generated.com.securboration.immortals.ontology.analysis.cg.callgraphedge import CallGraphEdge
from pymmortals.generated.com.securboration.immortals.ontology.analysis.profiling.properties.measuredproperty import MeasuredProperty
from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from typing import List


# noinspection PyPep8Naming
class DynamicCallGraph(MeasuredProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 observedInvocations: List[CallGraphEdge] = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(truthConstraint=truthConstraint)
        self.observedInvocations = observedInvocations
