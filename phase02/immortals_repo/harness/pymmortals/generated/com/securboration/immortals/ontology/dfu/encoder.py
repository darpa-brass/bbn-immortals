from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.dfu.dfu import Dfu
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.encodeddatatype import EncodedDataType
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionality import Functionality
from pymmortals.generated.com.securboration.immortals.ontology.lang.compiledcodeunit import CompiledCodeUnit
from typing import List
from typing import Type


# noinspection PyPep8Naming
class Encoder(Dfu):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 codeUnit: CompiledCodeUnit = None,
                 functionalityBeingPerformed: Functionality = None,
                 inputType: DataType = None,
                 outputType: EncodedDataType = None,
                 resourceDependencies: List[Type[Resource]] = None):
        super().__init__(codeUnit=codeUnit, functionalityBeingPerformed=functionalityBeingPerformed, resourceDependencies=resourceDependencies)
        self.inputType = inputType
        self.outputType = outputType
