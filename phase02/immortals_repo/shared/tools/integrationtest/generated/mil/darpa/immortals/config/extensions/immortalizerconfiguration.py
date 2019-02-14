from integrationtest.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class ImmortalizerConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 identifier: str = None,
                 performBuildFileAnalysis: bool = None,
                 performDslCompilation: bool = None,
                 performKrgpBytecodeAnalysis: bool = None,
                 performKrgpCompleteGradleTaskAnalysis: bool = None,
                 performSchemaAnalysis: bool = None,
                 performTestCoverageAnalysis: bool = None,
                 producedDataTargetFile: str = None):
        super().__init__()
        self.identifier = identifier
        self.performBuildFileAnalysis = performBuildFileAnalysis
        self.performDslCompilation = performDslCompilation
        self.performKrgpBytecodeAnalysis = performKrgpBytecodeAnalysis
        self.performKrgpCompleteGradleTaskAnalysis = performKrgpCompleteGradleTaskAnalysis
        self.performSchemaAnalysis = performSchemaAnalysis
        self.performTestCoverageAnalysis = performTestCoverageAnalysis
        self.producedDataTargetFile = producedDataTargetFile
