"""
The root configuration for the python-based components of Immortals
"""

# noinspection PyPep8Naming
import json
import os
from threading import RLock
from typing import List, Dict

from .serializable import Serializable


# noinspection PyPep8Naming
class RepositoryServiceConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 root: str,
                 executableFile: str,
                 port: int):
        super().__init__()
        self.root = root
        self.executableFile = executableFile
        self.port = port


# noinspection PyPep8Naming
class DasServiceConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 root: str,
                 executableFile: str,
                 port: int,
                 websocketPort: int):
        super().__init__()
        self.root = root
        self.executableFile = executableFile
        self.port = port
        self.websocketPort = websocketPort


# noinspection PyPep8Naming
class FusekiServiceConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 root: str,
                 port: int):
        super().__init__()
        self.root = root
        self.port = port


# noinspection PyPep8Naming
class ValidationProgramConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 root: str,
                 executableFile: str,
                 baseParameters: List[str]):
        super().__init__()
        self.root = root
        self.executableFile = executableFile
        self.baseParameters = baseParameters


# noinspection PyPep8Naming
class Lifecycle(Serializable):
    _validator_values = {}

    def __init__(self,
                 setupEnvironment: bool,
                 setupApplications: bool,
                 executeScenario: bool,
                 haltEnvironment: bool
                 ):
        super().__init__()
        self.setupEnvironment = setupEnvironment
        self.setupApplications = setupApplications
        self.executeScenario = executeScenario
        self.haltEnvironment = haltEnvironment


# noinspection PyPep8Naming
class SetupEnvironmentLifecycle(Serializable):
    _validator_values = {}

    def __init__(self,
                 destroyExisting: bool,
                 cleanExisting: bool):
        super().__init__()
        self.destroyExisting = destroyExisting
        self.cleanExisting = cleanExisting


# noinspection PyPep8Naming
class ValidationEnvironmentConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 displayAndroidEmulatorGui: bool,
                 startAndroidEmulatorsSimultaneously: bool,
                 lifecycle: Lifecycle,
                 setupEnvironmentLifecycle: SetupEnvironmentLifecycle,
                 androidEmulatorQemuArgs: List[str],
                 initialEmulatorCount: int,
                 startServer: bool):
        super().__init__()
        self.displayAndroidEmulatorGui = displayAndroidEmulatorGui
        self.lifecycle = lifecycle
        self.setupEnvironmentLifecycle = setupEnvironmentLifecycle
        self.startAndroidEmulatorsSimultaneously = startAndroidEmulatorsSimultaneously
        self.androidEmulatorQemuArgs = androidEmulatorQemuArgs
        self.initialEmulatorCount = initialEmulatorCount
        self.startServer = startServer


class ImmortalizationTarget(Serializable):
    _validator_values = {}

    def __init__(self,
                 path: str):
        super().__init__()
        self.path = path


# noinspection PyPep8Naming
class DeploymentEnvironment(Serializable):
    _validator_values = {}

    def __init__(self,
                 identifier: str,
                 sdkLevel: int,
                 deploymentPlatform: str):
        super().__init__()
        self.identifier = identifier
        self.sdkLevel = sdkLevel
        self.deploymentPlatform = deploymentPlatform


class DockerCoreConfig(Serializable):
    _validator_values = {}

    def __init__(self,
                 scripts: List[str]
                 ):
        super().__init__()
        self.scripts = scripts


# noinspection PyPep8Naming
class ScenarioRunnerCoreConfig(Serializable):
    _validator_values = {}

    def __init__(self,
                 docker: DockerCoreConfig):
        super().__init__()
        self.docker = docker


# noinspection PyPep8Naming
class TestHarnessConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 enabled: bool,
                 protocol: str,
                 url: str,
                 port: int):
        super().__init__()
        self.enabled = enabled
        self.protocol = protocol
        self.url = url
        self.port = port


# noinspection PyPep8Naming
class TestAdapterConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 enabled: bool,
                 protocol: str,
                 url: str,
                 port: int,
                 executableFile: str,
                 reportRawData: bool):
        super().__init__()
        self.enabled = enabled
        self.protocol = protocol
        self.url = url
        self.port = port
        self.executableFile = executableFile
        self.reportRawData = reportRawData


# noinspection PyPep8Naming
class ValidationConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 minimumTestDurationMS: int,
                 pcapyMonitorInterface: str,
                 pcapySnapshotLength: int,
                 pcapyPromiscuousMode: bool,
                 pcapyPollingIntervalMS: int,
                 pcapySamplingIntervalMS: int,
                 pcapyMonitorPort: int,
                 bandwidthMonitorReportingIntervalMS: int,
                 bandwidthValidatorSampleDurationMultiplier: int):
        super().__init__()
        self.minimumTestDurationMS = minimumTestDurationMS
        self.pcapyMonitorInterface = pcapyMonitorInterface
        self.pcapySnapshotLength = pcapySnapshotLength
        self.pcapyPromiscuousMode = pcapyPromiscuousMode
        self.pcapyPollingIntervalMS = pcapyPollingIntervalMS
        self.pcapySamplingIntervalMS = pcapySamplingIntervalMS
        self.pcapyMonitorPort = pcapyMonitorPort
        self.bandwidthMonitorReportingIntervalMS = bandwidthMonitorReportingIntervalMS
        self.bandwidthValidatorSampleDurationMultiplier = bandwidthValidatorSampleDurationMultiplier


# noinspection PyPep8Naming
class VisualizationConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 enabled: bool,
                 enableImmortalsDashboard: bool,
                 enableBandwidthCalculationsStaticDashboard: bool,
                 enableTimingDashboard: bool,
                 emulatorTimingDashboardFilepath: str = None):
        super().__init__()
        self.enabled = enabled
        self.enableImmortalsDashboard = enableImmortalsDashboard
        self.enableBandwidthCalculationsStaticDashboard = enableBandwidthCalculationsStaticDashboard
        self.enableTimingDashboard = enableTimingDashboard
        self.emulatorTimingDashboardFilepath = emulatorTimingDashboardFilepath


# noinspection PyPep8Naming
class DebugConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 routing: bool):
        super().__init__()
        self.routing = routing


# noinspection PyPep8Naming
class Configuration(Serializable):
    _validator_values = {}

    def __init__(self,
                 immortalsRoot: str,
                 runtimeRoot: str,
                 resultRoot: str,
                 dataRoot: str,
                 logFile: str,
                 artifactRoot: str,
                 repositoryRoot: str,
                 immortalizationTarget: ImmortalizationTarget,
                 deploymentEnvironments: List[DeploymentEnvironment],
                 scenarioRunner: ScenarioRunnerCoreConfig,
                 fuseki: FusekiServiceConfiguration,
                 repositoryService: RepositoryServiceConfiguration,
                 dasService: DasServiceConfiguration,
                 validationProgram: ValidationProgramConfiguration,
                 testHarness: TestHarnessConfiguration,
                 testAdapter: TestAdapterConfiguration,
                 validation: ValidationConfiguration,
                 validationEnvironment: ValidationEnvironmentConfiguration,
                 visualization: VisualizationConfiguration,
                 debugMode: bool,
                 throwExceptions: bool,
                 swallowAndShutdownOnException: bool,
                 debug: DebugConfiguration):
        super().__init__()
        self.immortalsRoot = immortalsRoot
        self.runtimeRoot = runtimeRoot
        self.resultRoot = resultRoot
        self.dataRoot = dataRoot
        self.logFile = logFile
        self.artifactRoot = artifactRoot
        self.repositoryRoot = repositoryRoot
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
        self.visualization = visualization
        self.debugMode = debugMode
        self.throwExceptions = throwExceptions
        self.swallowAndShutdownOnException = swallowAndShutdownOnException
        self.debug = debug


_configuration = None
_load_lock = RLock()


def _load_configuration(overrides=None):
    """
    :type overrides: dict[str, str] or None
    :rtype: Configuration
    """
    with _load_lock:
        if overrides is None:
            overrides = {}

        # TODO: I should probably be smarter with this...
        lines_string = ''

        with open(os.path.join(os.getcwd(), 'pymmortals/root_configuration.json')) as f:
            for line in iter(f):  # type: str

                if not line.lstrip(' ').startswith('#') and not line.lstrip(' ').startswith('//'):
                    lines_string += line

        configuration_d = json.loads(lines_string)

        if 'immortalsRoot' in configuration_d:
            configuration_d['immortalsRoot'] = os.path.abspath(configuration_d['immortalsRoot']) + '/'

        if 'immortalsRoot' not in configuration_d:
            if 'IMMORTALS_ROOT' in os.environ:
                configuration_d['immortalsRoot'] = os.environ['IMMORTALS_ROOT']
            else:
                configuration_d['immortalsRoot'] = os.path.join(os.getcwd(), '../')

        override_filepath = os.getenv('IMMORTALS_OVERRIDES')

        if override_filepath is None or not os.path.exists(override_filepath):
            override_filepath = os.path.join(configuration_d['dataRoot'], 'environment.json')

        if os.path.exists(override_filepath):
            override_lines_string = ''
            with open(override_filepath) as f:
                for line in iter(f):  # type: str
                    if not line.lstrip(' ').startswith('#') and not line.lstrip(' ').startswith('//'):
                        override_lines_string += line

            overrides.update(json.loads(override_lines_string))

        if len(list(overrides.keys())) > 0:
            for key in list(overrides.keys()):
                target_d = configuration_d
                override_path = key.split('.')
                override_tail = override_path.pop()

                for path_element in override_path:
                    target_d = target_d[path_element]

                target_d[override_tail] = overrides[key]

    return Configuration.from_dict(d=configuration_d)


def get_configuration(overrides: Dict[str, str] = None) -> Configuration:
    global _configuration

    if _configuration is None:
        _configuration = _load_configuration(overrides=overrides)

        roots = [
            _configuration.runtimeRoot,
            _configuration.resultRoot,
            _configuration.artifactRoot,
        ]
        for r in roots:
            if not os.path.exists(r):
                os.mkdir(r)

    return _configuration
