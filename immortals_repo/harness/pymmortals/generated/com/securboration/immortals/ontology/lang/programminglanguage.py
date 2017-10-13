from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.core.org.standard import Standard


# noinspection PyPep8Naming
class ProgrammingLanguage(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 languageName: str = None,
                 programmingLanguageStandard: Standard = None,
                 versionTag: str = None):
        super().__init__()
        self.languageName = languageName
        self.programmingLanguageStandard = programmingLanguageStandard
        self.versionTag = versionTag
