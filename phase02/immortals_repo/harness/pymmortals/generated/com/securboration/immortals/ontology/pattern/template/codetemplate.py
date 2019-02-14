from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.pattern.spec.libraryfunctionalaspectspec import LibraryFunctionalAspectSpec


# noinspection PyPep8Naming
class CodeTemplate(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 templateSpec: LibraryFunctionalAspectSpec = None,
                 templateSurfaceForm: str = None):
        super().__init__()
        self.templateSpec = templateSpec
        self.templateSurfaceForm = templateSurfaceForm
