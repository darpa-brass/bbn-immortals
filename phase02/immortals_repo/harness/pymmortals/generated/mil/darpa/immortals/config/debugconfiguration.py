from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class DebugConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 haltTestingOnFailure: bool = None,
                 keepRunningOnTestAdapterDoneSignal: bool = None,
                 logNetworkActivityToSeparateFile: bool = None,
                 loggingConfigDirectory: str = None,
                 shutdownGracePeriodMS: int = None,
                 useMockApplicationDeployment: bool = None,
                 useMockAqlBrass: bool = None,
                 useMockDas: bool = None,
                 useMockExtensionHddRass: bool = None,
                 useMockExtensionSchemaEvolution: bool = None,
                 useMockFuseki: bool = None,
                 useMockKnowledgeRepository: bool = None,
                 useMockTestAdapter: bool = None,
                 useMockTestCoordinators: bool = None,
                 useMockTestHarness: bool = None):
        super().__init__()
        self.haltTestingOnFailure = haltTestingOnFailure
        self.keepRunningOnTestAdapterDoneSignal = keepRunningOnTestAdapterDoneSignal
        self.logNetworkActivityToSeparateFile = logNetworkActivityToSeparateFile
        self.loggingConfigDirectory = loggingConfigDirectory
        self.shutdownGracePeriodMS = shutdownGracePeriodMS
        self.useMockApplicationDeployment = useMockApplicationDeployment
        self.useMockAqlBrass = useMockAqlBrass
        self.useMockDas = useMockDas
        self.useMockExtensionHddRass = useMockExtensionHddRass
        self.useMockExtensionSchemaEvolution = useMockExtensionSchemaEvolution
        self.useMockFuseki = useMockFuseki
        self.useMockKnowledgeRepository = useMockKnowledgeRepository
        self.useMockTestAdapter = useMockTestAdapter
        self.useMockTestCoordinators = useMockTestCoordinators
        self.useMockTestHarness = useMockTestHarness
