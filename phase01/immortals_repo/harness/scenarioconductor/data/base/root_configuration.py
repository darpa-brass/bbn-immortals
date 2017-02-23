# noinspection PyPep8Naming
import copy
import os

import commentjson as json

from .serializable import Serializable
from .tools import fillout_object


# noinspection PyPep8Naming
class RepositoryServiceConfiguration:
    """
    :type root: str
    :type executableFile: str
    type port: int
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self, root, executableFile, port):
        self.root = root
        self.executableFile = executableFile
        self.port = port


# noinspection PyPep8Naming
class DasServiceConfiguration:
    """
    :type root: str
    :type executableFile: str
    type port: int
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self, root, executableFile, port):
        self.root = root
        self.executableFile = executableFile
        self.port = port


# noinspection PyPep8Naming
class FusekiServiceConfiguration:
    """
    :type root: str
    type port: int
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self, root, port):
        self.root = root
        self.port = port


# noinspection PyPep8Naming
class ValidationProgramConfiguration:
    """
    :type root: str
    :type executableFile: str
    type baseParameters: list[str]
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self, root, executableFile, baseParameters):
        self.root = root
        self.executableFile = executableFile
        self.baseParameters = baseParameters


# noinspection PyPep8Naming
class Lifecycle:
    """
    :type setupEnvironment: bool
    :type setupApplications: bool
    :type executeScenario: bool
    :type haltEnvironment: bool
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
            setupEnvironment=d['setupEnvironment'],
            setupApplications=d['setupApplications'],
            executeScenario=d['executeScenario'],
            haltEnvironment=d['haltEnvironment'],
        )

    def __init__(self,
                 setupEnvironment,
                 setupApplications,
                 executeScenario,
                 haltEnvironment
                 ):
        self.setupEnvironment = setupEnvironment
        self.setupApplications = setupApplications
        self.executeScenario = executeScenario
        self.haltEnvironment = haltEnvironment

    def to_dict(self):
        return {
            'setupEnvironment': self.setupEnvironment,
            'setupApplications': self.setupApplications,
            'executeScenario': self.executeScenario,
            'haltEnvironment': self.haltEnvironment
        }


# noinspection PyPep8Naming
class SetupEnvironmentLifecycle:
    """
    :type destroyExisting: bool
    :type cleanExisting: bool
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
            destroyExisting=d['destroyExisting'],
            cleanExisting=d['cleanExisting'],
        )

    def __init__(self, destroyExisting, cleanExisting):
        self.destroyExisting = destroyExisting
        self.cleanExisting = cleanExisting

    def to_dict(self):
        return {
            'destroyExisting': self.destroyExisting,
            'cleanExisting': self.cleanExisting
        }


# noinspection PyPep8Naming
class ValidationEnvironmentConfiguration:
    """
    :type displayAndroidEmulatorGui: bool
    :type startAndroidEmulatorsSimultaneously: bool
    :type lifecycle: Lifecycle
    :type setupEnvironmentLifecycle: SetupEnvironmentLifecycle
    """

    @classmethod
    def from_dict(cls, d):
        dc = copy.deepcopy(d)
        dc['lifecycle'] = Lifecycle.from_dict(d['lifecycle'])
        dc['setupEnvironmentLifecycle'] = SetupEnvironmentLifecycle.from_dict(d['setupEnvironmentLifecycle'])
        return cls(**dc)

    def __init__(self, displayAndroidEmulatorGui, startAndroidEmulatorsSimultaneously, lifecycle, setupEnvironmentLifecycle):
        self.displayAndroidEmulatorGui = displayAndroidEmulatorGui
        self.lifecycle = lifecycle
        self.setupEnvironmentLifecycle = setupEnvironmentLifecycle
        self.startAndroidEmulatorsSimultaneously = startAndroidEmulatorsSimultaneously


class ImmortalizationTarget:
    """
    :type path: str
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self, path):
        self.path = path


# noinspection PyPep8Naming
class Configuration:
    """
    :type runtimeRoot: str
    :type resultRoot: str
    :type dataRoot: str
    :type logRoot: str
    :type artifactRoot: str
    :type immortalizationTarget: ImmortalizationTarget
    :type deploymentEnvironments: list[DeploymentEnvironment]
    :type scenarioRunner: ScenarioRunnerCoreConfig
    :type fuseki: FusekiServiceConfiguration
    :type repositoryService: RepositoryServiceConfiguration
    :type dasService: DasServiceConfiguration
    :type validationProgram: ValidationProgramConfiguration
    :type testHarness: TestHarnessConfiguration
    :type testAdapter: TestAdapterConfiguration
    :type validation: ValidationConfiguration
    :type validationEnvironment: ValidationEnvironmentConfiguration
    :type visualizationConfiguration: VisualizationConfiguration
    :type debugMode: bool
    """

    @classmethod
    def from_dict(cls, d, value_pool=None):
        if value_pool is None:
            value_pool = {}

        dc = copy.deepcopy(d)
        dc['immortalizationTarget'] = ImmortalizationTarget.from_dict(dc['immortalizationTarget'])
        fillout_object(dc['immortalizationTarget'], value_pool=value_pool)
        dc['deploymentEnvironments'] = map(lambda x: DeploymentEnvironment.from_dict(x), d['deploymentEnvironments'])
        dc['fuseki'] = FusekiServiceConfiguration.from_dict(dc['fuseki'])
        fillout_object(dc['fuseki'], value_pool=value_pool)
        dc['repositoryService'] = RepositoryServiceConfiguration.from_dict(dc['repositoryService'])
        fillout_object(dc['repositoryService'], value_pool=value_pool)
        dc['dasService'] = DasServiceConfiguration.from_dict(dc['dasService'])
        fillout_object(dc['dasService'], value_pool=value_pool)
        dc['validationProgram'] = ValidationProgramConfiguration.from_dict(dc['validationProgram'])
        fillout_object(dc['validationProgram'], value_pool=value_pool)
        dc['testHarness'] = TestHarnessConfiguration.from_dict(dc['testHarness'])
        fillout_object(dc['testHarness'], value_pool=value_pool)
        dc['testAdapter'] = TestAdapterConfiguration.from_dict(dc['testAdapter'])
        fillout_object(dc['testAdapter'], value_pool=value_pool)
        dc['validation'] = ValidationConfiguration.from_dict(dc['validation'])
        fillout_object(dc['validation'], value_pool=value_pool)
        dc['validationEnvironment'] = ValidationEnvironmentConfiguration.from_dict(dc['validationEnvironment'])
        fillout_object(dc['validationEnvironment'], value_pool=value_pool)
        dc['visualizationConfiguration'] = VisualizationConfiguration.from_dict(dc['visualizationConfiguration'])
        fillout_object(dc['visualizationConfiguration'], value_pool=value_pool)
        obj = cls(**dc)
        fillout_object(obj, value_pool=value_pool)
        return obj

    def __init__(self,
                 runtimeRoot,
                 resultRoot,
                 dataRoot,
                 logRoot,
                 artifactRoot,
                 immortalizationTarget,
                 deploymentEnvironments,
                 scenarioRunner,
                 fuseki,
                 repositoryService,
                 dasService,
                 validationProgram,
                 testHarness,
                 testAdapter,
                 validation,
                 validationEnvironment,
                 visualizationConfiguration,
                 debugMode):
        self.runtimeRoot = runtimeRoot
        self.resultRoot = resultRoot
        self.dataRoot = dataRoot
        self.logRoot = logRoot
        self.artifactRoot = artifactRoot
        self.immortalizationTarget = immortalizationTarget
        self.deploymentEnvironments = deploymentEnvironments
        self.scenarioRunner = scenarioRunner
        self.fuseki = fuseki
        self.repositoryService = repositoryService
        self.dasService = dasService
        self.validationProgram = validationProgram
        self.testHarness = testHarness
        self.testAdapter = testAdapter
        self.validation = validation
        self.validationEnvironment = validationEnvironment
        self.visualizationConfiguration = visualizationConfiguration
        self.debugMode = debugMode


# noinspection PyPep8Naming
class DeploymentEnvironment:
    """
    :type identifier: str
    :type: sdkLevel: str
    :type deploymentPlatform: str
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self, identifier, sdkLevel, deploymentPlatform):
        self.identifier = identifier
        self.sdkLevel = sdkLevel
        self.deploymentPlatform = deploymentPlatform


class DockerCoreConfig:
    """
    :type scripts: list[str]
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
            scripts=d['scripts']
        )

    def __init__(self,
                 scripts
                 ):
        self.scripts = scripts


# noinspection PyPep8Naming
class ScenarioRunnerCoreConfig:
    """
    :type configurationFile: str
    :type docker: DockerCoreConfig
    """

    @classmethod
    def from_dict(cls, d):
        dc = copy.deepcopy(d)
        dc['docker'] = DockerCoreConfig.from_dict(d['docker'])
        return cls(**dc)

    def __init__(self,
                 configurationFile,
                 docker):
        self.configurationFile = configurationFile
        self.docker = docker


# noinspection PyPep8Naming
class TestHarnessConfiguration:
    """
    :type enabled: bool
    :type protocol: str
    :type url: str
    :type port: int
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self,
                 enabled,
                 protocol,
                 url,
                 port):
        self.enabled = enabled
        self.protocol = protocol
        self.url = url
        self.port = port


# noinspection PyPep8Naming
class TestAdapterConfiguration:
    """
    :type enabled: bool
    :type protocol: str
    :type url: str
    :type port: int
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self,
                 enabled,
                 protocol,
                 url,
                 port):
        self.enabled = enabled
        self.protocol = protocol
        self.url = url
        self.port = port


# noinspection PyPep8Naming
class ValidationConfiguration:
    """
    :type minimumTestDurationMS: int
    :type pcapyMonitorInterface: str
    :type pcapySnapshotLength: int
    :type pcapyPromiscuousMode: bool
    :type pcapyPollingIntervalMS: int
    :type bandwidthMonitorReportingIntervalMS: int
    :type bandwidthValidatorSampleDurationMultiplier: int
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self, minimumTestDurationMS, pcapyMonitorInterface, pcapySnapshotLength, pcapyPromiscuousMode,
                 pcapyPollingIntervalMS, bandwidthMonitorReportingIntervalMS,
                 bandwidthValidatorSampleDurationMultiplier):
        self.minimumTestDurationMS = minimumTestDurationMS
        self.pcapyMonitorInterface = pcapyMonitorInterface
        self.pcapySnapshotLength = pcapySnapshotLength
        self.pcapyPromiscuousMode = pcapyPromiscuousMode
        self.pcapyPollingIntervalMS = pcapyPollingIntervalMS
        self.bandwidthMonitorReportingIntervalMS = bandwidthMonitorReportingIntervalMS
        self.bandwidthValidatorSampleDurationMultiplier = bandwidthValidatorSampleDurationMultiplier


# noinspection PyPep8Naming
class VisualizationConfiguration(Serializable):
    """
    :type enabled: bool
    """

    _types = {}

    def __init__(self, enabled):
        self.enabled = enabled


def load_configuration():
    configuration_path = os.path.join(os.getcwd(), 'scenarioconductor/root_configuration.json')

    configuration_d = json.load(open(configuration_path))

    override_filepath = os.path.join(configuration_d['dataRoot'], 'environment.json')
    if os.path.exists(override_filepath):
        override_configuration_d = json.load(open(override_filepath))

        for key in override_configuration_d:
            target_d = configuration_d
            override_path = key.split('.')
            override_tail = override_path.pop()

            for path_element in override_path:
                target_d = target_d[path_element]

            target_d[override_tail] = override_configuration_d[key]

    return Configuration.from_dict(
        configuration_d,
        value_pool={'immortalsRoot': os.path.join(os.getcwd(), '../')}
    )


demo_mode = load_configuration().visualizationConfiguration.enabled
debug_mode = load_configuration().debugMode
