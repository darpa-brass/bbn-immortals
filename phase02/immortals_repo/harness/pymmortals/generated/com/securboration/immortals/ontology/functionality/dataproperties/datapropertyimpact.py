from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.impacttype import ImpactType
from pymmortals.generated.com.securboration.immortals.ontology.functionality.datatype.dataproperty import DataProperty
from typing import Type


# noinspection PyPep8Naming
class DataPropertyImpact(DataProperty):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 hidden: bool = None,
                 impact: ImpactType = None,
                 propertyType: Type[DataProperty] = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(hidden=hidden, truthConstraint=truthConstraint)
        self.impact = impact
        self.propertyType = propertyType
