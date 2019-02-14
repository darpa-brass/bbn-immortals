from integrationtest.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class PartialLibraryUpdatesConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 exePath: str = None,
                 gitRepositoryUrl: str = None,
                 identifier: str = None,
                 targetClonePath: str = None,
                 workingDirectoryTemplateFolder: str = None):
        super().__init__()
        self.exePath = exePath
        self.gitRepositoryUrl = gitRepositoryUrl
        self.identifier = identifier
        self.targetClonePath = targetClonePath
        self.workingDirectoryTemplateFolder = workingDirectoryTemplateFolder
