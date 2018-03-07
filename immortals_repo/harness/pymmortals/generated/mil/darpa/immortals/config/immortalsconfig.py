from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.config.buildconfiguration import BuildConfiguration
from pymmortals.generated.mil.darpa.immortals.config.dasserviceconfiguration import DasServiceConfiguration
from pymmortals.generated.mil.darpa.immortals.config.debugconfiguration import DebugConfiguration
from pymmortals.generated.mil.darpa.immortals.config.deploymentenvironmentconfiguration import DeploymentEnvironmentConfiguration
from pymmortals.generated.mil.darpa.immortals.config.extensionsconfiguration import ExtensionsConfiguration
from pymmortals.generated.mil.darpa.immortals.config.fusekiconfiguration import FusekiConfiguration
from pymmortals.generated.mil.darpa.immortals.config.globalsconfig import GlobalsConfig
from pymmortals.generated.mil.darpa.immortals.config.knowledgerepoconfiguration import KnowledgeRepoConfiguration
from pymmortals.generated.mil.darpa.immortals.config.testadapterconfiguration import TestAdapterConfiguration
from pymmortals.generated.mil.darpa.immortals.config.testharnessconfiguration import TestHarnessConfiguration


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
                 knowledgeRepoService: KnowledgeRepoConfiguration = None,
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
        self.knowledgeRepoService = knowledgeRepoService
        self.testAdapter = testAdapter
        self.testHarness = testHarness
