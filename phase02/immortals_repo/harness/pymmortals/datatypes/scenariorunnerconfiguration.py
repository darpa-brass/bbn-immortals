import logging
from copy import deepcopy
from typing import List, Dict

from pymmortals.utils import path_helper
from .root_configuration import get_configuration
from .serializable import Serializable


# noinspection PyPep8Naming
class JsonFileOverrideConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 sourceFilepath: str,
                 overridePairs: Dict[str, str]):
        super().__init__()
        self.sourceFilepath: str = sourceFilepath
        self.overridePairs: Dict[str, str] = overridePairs


# noinspection PyPep8Naming
class FileCopyConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 sourceFilepath: str,
                 targetFilepath: str):
        super().__init__()
        self.sourceFilepath: str = sourceFilepath
        self.targetFilepath: str = targetFilepath


# noinspection PyPep8Naming
class ApplicationConfig(Serializable):
    _validator_values = {}

    def __init__(self,
                 applicationIdentifier: str,
                 instanceIdentifier: str,
                 deploymentPlatformEnvironment: str,
                 buildRoot: str,
                 executableFile: str,
                 applicationDeploymentDirectory: str,
                 filesToCopy: List[FileCopyConfiguration],
                 filesForCleanup: List[str],
                 configurationOverrides: List[JsonFileOverrideConfiguration]
                 ):
        super().__init__()
        self.applicationIdentifier = applicationIdentifier
        self.instanceIdentifier = instanceIdentifier
        self.deploymentPlatformEnvironment = deploymentPlatformEnvironment
        self.buildRoot = buildRoot
        self.executableFile = executableFile
        self.applicationDeploymentDirectory = applicationDeploymentDirectory
        self.filesToCopy = filesToCopy

        self.filesForCleanup = filesForCleanup
        self.configurationOverrides = configurationOverrides

        config = get_configuration()

        for f in filesToCopy:
            f.sourceFilepath = path_helper(True, config.immortalsRoot, f.sourceFilepath)

        for val in configurationOverrides:  # type: JsonFileOverrideConfiguration
            val.sourceFilepath = path_helper(True, config.immortalsRoot, val.sourceFilepath)

        self.buildRoot = path_helper(False, config.immortalsRoot, self.buildRoot)
        self.executableFile = path_helper(False, config.immortalsRoot, self.executableFile)

    @classmethod
    def from_dict(cls, d: Dict[str, object], value_pool: Dict[str, object] = None, do_replacement: bool = True):
        applicationIdentifier = d['applicationIdentifier']

        if applicationIdentifier == 'marti':
            return JavaApplicationConfig._from_dict(source_dict=d,
                                                    top_level_deserialization=True,
                                                    value_pool=value_pool,
                                                    object_map=None,
                                                    do_replacement=do_replacement)

        elif applicationIdentifier == 'ataklite':
            return AndroidApplicationConfig._from_dict(source_dict=d,
                                                       top_level_deserialization=True,
                                                       value_pool=value_pool,
                                                       object_map=None,
                                                       do_replacement=do_replacement)

        else:
            raise Exception('Unexpected applicationIdentifier identifier \'' + str(applicationIdentifier) + '\'!')


# noinspection PyPep8Naming
class JavaApplicationConfig(ApplicationConfig):
    _validator_values = deepcopy(ApplicationConfig._validator_values)

    def __init__(self,
                 applicationIdentifier: str,
                 instanceIdentifier: str,
                 deploymentPlatformEnvironment: str,
                 buildRoot: str,
                 executableFile: str,
                 applicationDeploymentDirectory: str,
                 filesToCopy: List[FileCopyConfiguration],
                 filesForCleanup: List[str],
                 configurationOverrides: List[JsonFileOverrideConfiguration]):
        ApplicationConfig.__init__(self,
                                   applicationIdentifier=applicationIdentifier,
                                   instanceIdentifier=instanceIdentifier,
                                   deploymentPlatformEnvironment=deploymentPlatformEnvironment,
                                   buildRoot=buildRoot,
                                   executableFile=executableFile,
                                   applicationDeploymentDirectory=applicationDeploymentDirectory,
                                   filesToCopy=filesToCopy,
                                   filesForCleanup=filesForCleanup,
                                   configurationOverrides=configurationOverrides)


# noinspection PyPep8Naming
class AndroidApplicationConfig(ApplicationConfig):
    _validator_values = deepcopy(ApplicationConfig._validator_values)

    def __init__(self,
                 applicationIdentifier: str,
                 instanceIdentifier: str,
                 deploymentPlatformEnvironment: str,
                 buildRoot: str,
                 executableFile: str,
                 applicationDeploymentDirectory: str,
                 filesToCopy: List[FileCopyConfiguration],
                 filesForCleanup: List[str],
                 configurationOverrides: List[JsonFileOverrideConfiguration],
                 packageIdentifier: str,
                 mainActivity: str,
                 permissions: List[str]
                 ):
        super().__init__(
            applicationIdentifier=applicationIdentifier,
            instanceIdentifier=instanceIdentifier,
            deploymentPlatformEnvironment=deploymentPlatformEnvironment,
            buildRoot=buildRoot,
            executableFile=executableFile,
            applicationDeploymentDirectory=applicationDeploymentDirectory,
            filesToCopy=filesToCopy,
            filesForCleanup=filesForCleanup,
            configurationOverrides=configurationOverrides
        )

        self.packageIdentifier: str = packageIdentifier
        self.mainActivity: str = mainActivity
        self.permissions: List[str] = permissions


# noinspection PyPep8Naming
class ScenarioConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 scenarioIdentifier: str,
                 durationMS: int,
                 deploymentApplications: List[ApplicationConfig],
                 validatorIdentifiers: List[str]
                 ):
        super().__init__()
        self.scenarioIdentifier: str = scenarioIdentifier
        self.durationMS: int = durationMS
        self.deploymentApplications: List[ApplicationConfig] = deploymentApplications
        self.validatorIdentifiers: List[str] = validatorIdentifiers


# noinspection PyPep8Naming
class ScenarioRunnerConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 sessionIdentifier: str,
                 deploymentDirectory: str,
                 scenario: ScenarioConfiguration,
                 validate: bool,
                 ):
        super().__init__()
        self.sessionIdentifier: str = sessionIdentifier
        self.deploymentDirectory: str = deploymentDirectory
        self.scenario: ScenarioConfiguration = scenario
        self.validate: bool = validate

        logging.info(self.to_json_str_pretty())
