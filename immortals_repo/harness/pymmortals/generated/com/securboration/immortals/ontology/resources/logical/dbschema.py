from pymmortals.generated.com.securboration.immortals.ontology.core.humanreadable import HumanReadable
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.relationship.relation import Relation
from pymmortals.generated.com.securboration.immortals.ontology.resources.logical.schema import Schema
from typing import List


# noinspection PyPep8Naming
class DBSchema(HumanReadable, Schema):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDesc: str = None,
                 humanReadableDescription: str = None,
                 name: str = None,
                 relations: List[Relation] = None,
                 resourceProperty: List[Property] = None,
                 version: str = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.humanReadableDesc = humanReadableDesc
        self.name = name
        self.relations = relations
        self.version = version
