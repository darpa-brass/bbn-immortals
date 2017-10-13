from pymmortals.generated.com.securboration.immortals.ontology.analysis.dataflownode import DataflowNode
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource


# noinspection PyPep8Naming
class IntraProcessDataflowNode(DataflowNode):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 contextTemplate: Resource = None,
                 resourceTemplate: Resource = None):
        super().__init__(contextTemplate=contextTemplate, resourceTemplate=resourceTemplate)
