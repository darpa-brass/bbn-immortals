from pymmortals.generated.com.securboration.immortals.ontology.analysis.interprocessdataflownode import InterProcessDataflowNode
from pymmortals.generated.com.securboration.immortals.ontology.analysis.interprocessfunctionalityentry import InterProcessFunctionalityEntry
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource


# noinspection PyPep8Naming
class InterMethodDataflowNode(InterProcessDataflowNode):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 contextTemplate: Resource = None,
                 entry: InterProcessFunctionalityEntry = None,
                 javaClassName: str = None,
                 javaMethodName: str = None,
                 javaMethodPointer: str = None,
                 resourceTemplate: Resource = None):
        super().__init__(contextTemplate=contextTemplate, entry=entry, resourceTemplate=resourceTemplate)
        self.javaClassName = javaClassName
        self.javaMethodName = javaMethodName
        self.javaMethodPointer = javaMethodPointer
