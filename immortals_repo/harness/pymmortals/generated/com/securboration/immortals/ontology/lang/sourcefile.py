from pymmortals.generated.com.securboration.immortals.ontology.lang.codeunit import CodeUnit
from pymmortals.generated.com.securboration.immortals.ontology.lang.programminglanguage import ProgrammingLanguage


# noinspection PyPep8Naming
class SourceFile(CodeUnit):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 fileSystemPath: str = None,
                 languageModel: ProgrammingLanguage = None,
                 repositoryPath: str = None,
                 source: str = None):
        super().__init__()
        self.fileSystemPath = fileSystemPath
        self.languageModel = languageModel
        self.repositoryPath = repositoryPath
        self.source = source
