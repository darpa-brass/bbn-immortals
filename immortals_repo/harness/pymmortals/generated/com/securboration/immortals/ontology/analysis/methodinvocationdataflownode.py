from pymmortals.generated.com.securboration.immortals.ontology.analysis.intraprocessdataflownode import IntraProcessDataflowNode
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource


# noinspection PyPep8Naming
class MethodInvocationDataflowNode(IntraProcessDataflowNode):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 contextTemplate: Resource = None,
                 javaClassName: str = None,
                 javaMethodName: str = None,
                 javaMethodPointer: str = None,
                 resourceTemplate: Resource = None):
        super().__init__(contextTemplate=contextTemplate, resourceTemplate=resourceTemplate)
        self.javaClassName = javaClassName
        self.javaMethodName = javaMethodName
        self.javaMethodPointer = javaMethodPointer
