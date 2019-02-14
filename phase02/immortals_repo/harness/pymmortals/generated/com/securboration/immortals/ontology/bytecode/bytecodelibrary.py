from pymmortals.generated.com.securboration.immortals.ontology.bytecode.bytecodeartifactcoordinate import BytecodeArtifactCoordinate
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from typing import List


# noinspection PyPep8Naming
class BytecodeLibrary(Resource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 humanReadableDescription: str = None,
                 library: BytecodeArtifactCoordinate = None,
                 resourceProperty: List[Property] = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.library = library
