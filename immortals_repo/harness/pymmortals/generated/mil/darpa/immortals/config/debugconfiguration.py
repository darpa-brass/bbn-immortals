from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class DebugConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 logNetworkActivityToSeparateFile: bool = None,
                 loggingConfigDirectory: str = None,
                 shutdownGracePeriodMS: int = None,
                 useMockApplicationDeployment: bool = None,
                 useMockAqlBrass: bool = None,
                 useMockDas: bool = None,
                 useMockExtensionSchemaEvolution: bool = None,
                 useMockFuseki: bool = None,
                 useMockKnowledgeRepository: bool = None,
                 useMockTestAdapter: bool = None,
                 useMockTestHarness: bool = None):
        super().__init__()
        self.logNetworkActivityToSeparateFile = logNetworkActivityToSeparateFile
        self.loggingConfigDirectory = loggingConfigDirectory
        self.shutdownGracePeriodMS = shutdownGracePeriodMS
        self.useMockApplicationDeployment = useMockApplicationDeployment
        self.useMockAqlBrass = useMockAqlBrass
        self.useMockDas = useMockDas
        self.useMockExtensionSchemaEvolution = useMockExtensionSchemaEvolution
        self.useMockFuseki = useMockFuseki
        self.useMockKnowledgeRepository = useMockKnowledgeRepository
        self.useMockTestAdapter = useMockTestAdapter
        self.useMockTestHarness = useMockTestHarness
