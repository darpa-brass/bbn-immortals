# noinspection PyPep8Naming
import copy

from .base.tools import fillout_object


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
class AndroidEmulatorConfiguration:
    """
    :type displayEmulaturGui: bool
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self, displayEmulatorGui):
        self.displayEmulatorGui = displayEmulatorGui


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
    :type androidEmulator: AndroidEmulatorConfiguration
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
        dc['androidEmulator'] = AndroidEmulatorConfiguration.from_dict(dc['androidEmulator'])
        fillout_object(dc['androidEmulator'], value_pool=value_pool)
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
                 androidEmulator):
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
        self.androidEmulator = androidEmulator


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
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self, minimumTestDurationMS):
        self.minimumTestDurationMS = minimumTestDurationMS
