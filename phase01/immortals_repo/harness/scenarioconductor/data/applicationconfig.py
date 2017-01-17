from copy import deepcopy


from .base.tools import path_helper, fillout_object
from ..immortalsglobals import IMMORTALS_ROOT


# noinspection PyPep8Naming
class ApplicationConfig:
    """
    :type applicationIdentifier: str
    :type instanceIdentifier: str
    :type deploymentPlatformEnvironment: str
    :type buildRoot: str
    :type executableFile: str
    :type applicationDeploymentDirectory: str
    :type files: dict[str,str]
    :type filesForCleanup: list[str]
    :type configurationCustomizations: dict[str,dict[str,str]]
    """

    @classmethod
    def from_json(cls, d, parent_config, value_pool=None):
        applicationIdentifier = d['applicationIdentifier']

        if applicationIdentifier == 'marti':
            return JavaApplicationConfig.from_dict(d=d, parent_config=parent_config, value_pool=value_pool)

        elif applicationIdentifier == 'ataklite':
            return AndroidApplicationConfig.from_dict(d=d, parent_config=parent_config, value_pool=value_pool)

        else:
            raise Exception('Unexpected applicationIdentifier identifier \'' + applicationIdentifier + '\'!')

    def __init__(self,
                 applicationIdentifier,
                 instanceIdentifier,
                 deploymentPlatformEnvironment,
                 buildRoot,
                 executableFile,
                 applicationDeploymentDirectory,
                 files,
                 filesForCleanup,
                 configurationCustomizations,
                 parent_config,
                 value_pool=None
                 ):

        self.parent_config = parent_config
        self.applicationIdentifier = applicationIdentifier
        self.instanceIdentifier = instanceIdentifier
        self.deploymentPlatformEnvironment = deploymentPlatformEnvironment
        self.buildRoot = buildRoot
        self.executableFile = executableFile
        self.applicationDeploymentDirectory = applicationDeploymentDirectory
        self.files = files

        self.filesForCleanup = filesForCleanup
        self.configurationCustomizations = configurationCustomizations

        fillout_object(self, {} if value_pool is None else value_pool)

        abs_files = {}
        for k in files.keys():
            abs_files[path_helper(True, IMMORTALS_ROOT, k)] = files[k]

        self.files = abs_files

        abs_files = {}
        for k in configurationCustomizations.keys():
            abs_files[path_helper(True, IMMORTALS_ROOT, k)] = configurationCustomizations[k]

        self.configurationCustomizations = abs_files

        self.buildRoot = path_helper(True, IMMORTALS_ROOT, self.buildRoot)
        self.executableFile = path_helper(True, IMMORTALS_ROOT, self.executableFile)


# noinspection PyPep8Naming
class JavaApplicationConfig(ApplicationConfig):
    @classmethod
    def from_dict(cls, d, parent_config, value_pool=None):
        return cls(parent_config=parent_config, value_pool=value_pool, **deepcopy(d))

    def __init__(self,
                 applicationIdentifier,
                 instanceIdentifier,
                 deploymentPlatformEnvironment,
                 buildRoot,
                 executableFile,
                 applicationDeploymentDirectory,
                 files,
                 filesForCleanup,
                 configurationCustomizations,
                 parent_config,
                 value_pool=None
                 ):
        ApplicationConfig.__init__(self,
                                   applicationIdentifier=applicationIdentifier,
                                   instanceIdentifier=instanceIdentifier,
                                   deploymentPlatformEnvironment=deploymentPlatformEnvironment,
                                   buildRoot=buildRoot,
                                   executableFile=executableFile,
                                   applicationDeploymentDirectory=applicationDeploymentDirectory,
                                   files=files,
                                   filesForCleanup=filesForCleanup,
                                   configurationCustomizations=configurationCustomizations,
                                   parent_config=parent_config,
                                   value_pool=value_pool)


# noinspection PyPep8Naming
class AndroidApplicationConfig(ApplicationConfig):
    """
    :type packageIdentifier: str
    :type mainActivity: str
    :type permissions: list[str]
    """

    @classmethod
    def from_dict(cls, d, parent_config, value_pool=None):
        """
        :type d dict
        :type parent_config: object
        :type value_pool: dict[str,str]
        """

        return cls(parent_config=parent_config, value_pool=value_pool, **deepcopy(d))

    def __init__(self,
                 applicationIdentifier,
                 instanceIdentifier,
                 deploymentPlatformEnvironment,
                 buildRoot,
                 executableFile,
                 applicationDeploymentDirectory,
                 files,
                 filesForCleanup,
                 configurationCustomizations,
                 packageIdentifier,
                 mainActivity,
                 permissions,
                 parent_config,
                 value_pool=None
                 ):
        ApplicationConfig.__init__(self,
                                   applicationIdentifier=applicationIdentifier,
                                   instanceIdentifier=instanceIdentifier,
                                   deploymentPlatformEnvironment=deploymentPlatformEnvironment,
                                   buildRoot=buildRoot,
                                   executableFile=executableFile,
                                   applicationDeploymentDirectory=applicationDeploymentDirectory,
                                   files=files,
                                   filesForCleanup=filesForCleanup,
                                   configurationCustomizations=configurationCustomizations,
                                   parent_config=parent_config,
                                   value_pool=value_pool
                                   )

        self.packageIdentifier = packageIdentifier
        self.mainActivity = mainActivity
        self.permissions = permissions
