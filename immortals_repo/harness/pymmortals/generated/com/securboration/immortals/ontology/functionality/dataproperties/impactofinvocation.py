from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.impacttype import ImpactType
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.dataproperty import DataProperty
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List
from typing import Type


# noinspection PyPep8Naming
class ImpactOfInvocation(DataProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 hidden: bool = None,
                 impactOfInvocation: ImpactType = None,
                 impactedProperties: List[Type[Property]] = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(hidden=hidden, truthConstraint=truthConstraint)
        self.impactOfInvocation = impactOfInvocation
        self.impactedProperties = impactedProperties
