from pymmortals.generated.com.securboration.immortals.ontology.analysis.dataflowedge import DataflowEdge
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.assertionbindingsite import AssertionBindingSite


# noinspection PyPep8Naming
class DataflowBindingSite(AssertionBindingSite):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 edge: DataflowEdge = None,
                 humanReadableDescription: str = None):
        super().__init__(humanReadableDescription=humanReadableDescription)
        self.edge = edge
