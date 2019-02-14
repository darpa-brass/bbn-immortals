from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.fidelity import Fidelity
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.impacttype import ImpactType
from typing import Type


# noinspection PyPep8Naming
class FidelityResourceRelationship(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 drivenFidelity: Type[Fidelity] = None,
                 drivingCondition: ImpactType = None,
                 impactOnResource: ImpactType = None,
                 impactedResource: Type[Resource] = None):
        super().__init__()
        self.drivenFidelity = drivenFidelity
        self.drivingCondition = drivingCondition
        self.impactOnResource = impactOnResource
        self.impactedResource = impactedResource
