from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.fm.consumption.consumptionqualifier import ConsumptionQualifier
from pymmortals.generated.com.securboration.immortals.ontology.fm.consumption.consumptionscope import ConsumptionScope
from typing import Type


# noinspection PyPep8Naming
class ResourceConsumption(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 consumedQuantityExpression: str = None,
                 consumedQuantityUnit: str = None,
                 consumedResourceType: Type[Resource] = None,
                 consumptionQualifier: ConsumptionQualifier = None,
                 consumptionScope: ConsumptionScope = None):
        super().__init__()
        self.consumedQuantityExpression = consumedQuantityExpression
        self.consumedQuantityUnit = consumedQuantityUnit
        self.consumedResourceType = consumedResourceType
        self.consumptionQualifier = consumptionQualifier
        self.consumptionScope = consumptionScope
