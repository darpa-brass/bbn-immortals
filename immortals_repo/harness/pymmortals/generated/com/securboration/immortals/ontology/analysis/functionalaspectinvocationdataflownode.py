from pymmortals.generated.com.securboration.immortals.ontology.analysis.methodinvocationdataflownode import MethodInvocationDataflowNode
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionalaspect import FunctionalAspect


# noinspection PyPep8Naming
class FunctionalAspectInvocationDataflowNode(MethodInvocationDataflowNode):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 aspectImplemented: FunctionalAspect = None,
                 contextTemplate: Resource = None,
                 javaClassName: str = None,
                 javaMethodName: str = None,
                 javaMethodPointer: str = None,
                 resourceTemplate: Resource = None):
        super().__init__(contextTemplate=contextTemplate, javaClassName=javaClassName, javaMethodName=javaMethodName, javaMethodPointer=javaMethodPointer, resourceTemplate=resourceTemplate)
        self.aspectImplemented = aspectImplemented
