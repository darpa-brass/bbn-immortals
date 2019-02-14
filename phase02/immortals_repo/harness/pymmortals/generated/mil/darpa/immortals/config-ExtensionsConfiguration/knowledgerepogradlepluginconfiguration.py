from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class KnowledgeRepoGradlePluginConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 identifier: str = None,
                 performBytecodeAnalysis: bool = None,
                 ttlTargetDirectory: str = None):
        super().__init__()
        self.identifier = identifier
        self.performBytecodeAnalysis = performBytecodeAnalysis
        self.ttlTargetDirectory = ttlTargetDirectory
