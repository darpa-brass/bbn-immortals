from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.config.buildconfiguration import BuildConfiguration
from integrationtest.generated.mil.darpa.immortals.config.dasserviceconfiguration import DasServiceConfiguration
from integrationtest.generated.mil.darpa.immortals.config.debugconfiguration import DebugConfiguration
from integrationtest.generated.mil.darpa.immortals.config.deploymentenvironmentconfiguration import DeploymentEnvironmentConfiguration
from integrationtest.generated.mil.darpa.immortals.config.extensionsconfiguration import ExtensionsConfiguration
from integrationtest.generated.mil.darpa.immortals.config.fusekiconfiguration import FusekiConfiguration
from integrationtest.generated.mil.darpa.immortals.config.globalsconfig import GlobalsConfig
from integrationtest.generated.mil.darpa.immortals.config.knowledgerepoconfiguration import KnowledgeRepoConfiguration
from integrationtest.generated.mil.darpa.immortals.config.testadapterconfiguration import TestAdapterConfiguration
from integrationtest.generated.mil.darpa.immortals.config.testharnessconfiguration import TestHarnessConfiguration
from typing import List


# noinspection PyPep8Naming
class ImmortalsConfig(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 build: BuildConfiguration = None,
                 dasService: DasServiceConfiguration = None,
                 debug: DebugConfiguration = None,
                 deploymentEnvironment: DeploymentEnvironmentConfiguration = None,
                 extensions: ExtensionsConfiguration = None,
                 fuseki: FusekiConfiguration = None,
                 globals: GlobalsConfig = None,
                 knowledgeBuilderClassNames: List[str] = None,
                 knowledgeBuilderGradleTasks: List[str] = None,
                 knowledgeRepoService: KnowledgeRepoConfiguration = None,
                 targetApplicationUris: List[str] = None,
                 testAdapter: TestAdapterConfiguration = None,
                 testHarness: TestHarnessConfiguration = None):
        super().__init__()
        self.build = build
        self.dasService = dasService
        self.debug = debug
        self.deploymentEnvironment = deploymentEnvironment
        self.extensions = extensions
        self.fuseki = fuseki
        self.globals = globals
        self.knowledgeBuilderClassNames = knowledgeBuilderClassNames
        self.knowledgeBuilderGradleTasks = knowledgeBuilderGradleTasks
        self.knowledgeRepoService = knowledgeRepoService
        self.targetApplicationUris = targetApplicationUris
        self.testAdapter = testAdapter
        self.testHarness = testHarness
