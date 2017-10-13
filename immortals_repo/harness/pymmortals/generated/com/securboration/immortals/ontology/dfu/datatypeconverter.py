from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.dfu.dfu import Dfu
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.datatype import DataType
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionality import Functionality
from pymmortals.generated.com.securboration.immortals.ontology.lang.compiledcodeunit import CompiledCodeUnit
from typing import List
from typing import Type


# noinspection PyPep8Naming
class DataTypeConverter(Dfu):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 codeUnit: CompiledCodeUnit = None,
                 fromDataType: DataType = None,
                 functionalityBeingPerformed: Functionality = None,
                 resourceDependencies: List[Type[Resource]] = None,
                 toDataType: DataType = None):
        super().__init__(codeUnit=codeUnit, functionalityBeingPerformed=functionalityBeingPerformed, resourceDependencies=resourceDependencies)
        self.fromDataType = fromDataType
        self.toDataType = toDataType
