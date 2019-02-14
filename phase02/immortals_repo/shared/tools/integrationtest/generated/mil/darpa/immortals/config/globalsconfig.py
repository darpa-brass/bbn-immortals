from integrationtest.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class GlobalsConfig(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 executionsDirectory: str = None,
                 globalApplicationDeploymentDirectory: str = None,
                 globalLogDirectory: str = None,
                 globalWorkingDirectory: str = None,
                 headless: bool = None,
                 immortalsOntologyUriPrefix: str = None,
                 immortalsOntologyUriRoot: str = None,
                 immortalsRepo: str = None,
                 immortalsRoot: str = None,
                 ttlIngestionDirectory: str = None):
        super().__init__()
        self.executionsDirectory = executionsDirectory
        self.globalApplicationDeploymentDirectory = globalApplicationDeploymentDirectory
        self.globalLogDirectory = globalLogDirectory
        self.globalWorkingDirectory = globalWorkingDirectory
        self.headless = headless
        self.immortalsOntologyUriPrefix = immortalsOntologyUriPrefix
        self.immortalsOntologyUriRoot = immortalsOntologyUriRoot
        self.immortalsRepo = immortalsRepo
        self.immortalsRoot = immortalsRoot
        self.ttlIngestionDirectory = ttlIngestionDirectory
