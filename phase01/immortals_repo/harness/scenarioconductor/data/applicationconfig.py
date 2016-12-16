from ..immortalsglobals import IMMORTALS_ROOT
from ..utils import path_helper, fillout_object


# noinspection PyPep8Naming
class ApplicationConfig:
    """
    :type applicationIdentifier: str
    :type instanceIdentifier: str
    :type deploymentPlatformEnvironment: str
    :type buildRoot: str
    :type executableFile: str
    :type applicationDeploymentDirectory: str
    :type configurationTemplateFilepath: str
    :type configurationTargetFilepath: str
    :type files: dict[str,str]
    :type filesForCleanup: list[str]
    :type properties: dict[str,str]
    """

    @classmethod
    def from_json(cls, j, parent_config, value_pool={}):
        applicationIdentifier = j['applicationIdentifier']

        if applicationIdentifier == 'marti':
            return JavaApplicationConfig.from_dict(j=j, parent_config=parent_config, value_pool=value_pool)

        elif applicationIdentifier == 'ataklite':
            return AndroidApplicationConfig.from_dict(j=j, parent_config=parent_config, value_pool=value_pool)

        else:
            raise Exception('Unexpected applicationIdentifier identifier \'' + applicationIdentifier + '\'!')

    def __init__(self,
                 applicationIdentifier,
                 instanceIdentifier,
                 deploymentPlatformEnvironment,
                 buildRoot,
                 executableFile,
                 applicationDeploymentDirectory,
                 configurationTemplateFilepath,
                 configurationTargetFilepath,
                 files,
                 filesForCleanup,
                 properties,
                 parent_config,
                 value_pool={}
                 ):

        self.parent_config = parent_config
        self.applicationIdentifier = applicationIdentifier
        self.instanceIdentifier = instanceIdentifier
        self.deploymentPlatformEnvironment = deploymentPlatformEnvironment
        self.buildRoot = buildRoot
        self.executableFile = executableFile
        self.applicationDeploymentDirectory = applicationDeploymentDirectory
        self.configurationTemplateFilepath = configurationTemplateFilepath
        self.configurationTargetFilepath = configurationTargetFilepath
        self.files = files

        self.filesForCleanup = filesForCleanup
        self.properties = properties

        fillout_object(self, value_pool)

        abs_files = {}
        for k in files.keys():
            abs_files[path_helper(True, IMMORTALS_ROOT, k)] = files[k]

        self.files = abs_files

        if self.configurationTemplateFilepath is not None:
            self.configurationTemplateFilepath = path_helper(False, IMMORTALS_ROOT, self.configurationTemplateFilepath)

        self.buildRoot = path_helper(True, IMMORTALS_ROOT, self.buildRoot)
        self.executableFile = path_helper(True, IMMORTALS_ROOT, self.executableFile)


# noinspection PyPep8Naming
class JavaApplicationConfig(ApplicationConfig):
    @classmethod
    def from_dict(cls, j, parent_config, value_pool={}):
        return cls(
                applicationIdentifier=j['applicationIdentifier'],
                instanceIdentifier=j['instanceIdentifier'],
                deploymentPlatformEnvironment=j['deploymentPlatformEnvironment'],
                buildRoot=j['buildRoot'],
                executableFile=j['executableFile'],
                applicationDeploymentDirectory=j['applicationDeploymentDirectory'],
                configurationTemplateFilepath=j['configurationTemplateFilepath'],
                configurationTargetFilepath=j['configurationTargetFilepath'],
                files=j['files'],
                filesForCleanup=j['filesForCleanup'],
                properties=j['properties'],
                parent_config=parent_config,
                value_pool=value_pool
        )

    def __init__(self,
                 applicationIdentifier,
                 instanceIdentifier,
                 deploymentPlatformEnvironment,
                 buildRoot,
                 executableFile,
                 applicationDeploymentDirectory,
                 configurationTemplateFilepath,
                 configurationTargetFilepath,
                 files,
                 filesForCleanup,
                 properties,
                 parent_config,
                 value_pool={}
                 ):
        ApplicationConfig.__init__(self,
                                   applicationIdentifier=applicationIdentifier,
                                   instanceIdentifier=instanceIdentifier,
                                   deploymentPlatformEnvironment=deploymentPlatformEnvironment,
                                   buildRoot=buildRoot,
                                   executableFile=executableFile,
                                   applicationDeploymentDirectory=applicationDeploymentDirectory,
                                   configurationTemplateFilepath=configurationTemplateFilepath,
                                   configurationTargetFilepath=configurationTargetFilepath,
                                   files=files,
                                   filesForCleanup=filesForCleanup,
                                   properties=properties,
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
    def from_dict(cls, j, parent_config, value_pool={}):
        """
        :type j dict
        :type parent_config: object
        :type value_pool: dict[str,str]
        """
        return cls(applicationIdentifier=j['applicationIdentifier'],
                   instanceIdentifier=j['instanceIdentifier'],
                   deploymentPlatformEnvironment=j['deploymentPlatformEnvironment'],
                   buildRoot=j['buildRoot'],
                   executableFile=j['executableFile'],
                   applicationDeploymentDirectory=j['applicationDeploymentDirectory'],
                   configurationTemplateFilepath=j['configurationTemplateFilepath'],
                   configurationTargetFilepath=j['configurationTargetFilepath'],
                   files=j['files'],
                   filesForCleanup=j['filesForCleanup'],
                   properties=j['properties'],
                   packageIdentifier=j['packageIdentifier'],
                   mainActivity=j['mainActivity'],
                   permissions=j['permissions'],
                   parent_config=parent_config,
                   value_pool=value_pool
                   )

    def __init__(self,
                 applicationIdentifier,
                 instanceIdentifier,
                 deploymentPlatformEnvironment,
                 buildRoot,
                 executableFile,
                 applicationDeploymentDirectory,
                 configurationTemplateFilepath,
                 configurationTargetFilepath,
                 files,
                 filesForCleanup,
                 properties,
                 packageIdentifier,
                 mainActivity,
                 permissions,
                 parent_config,
                 value_pool={}
                 ):
        ApplicationConfig.__init__(self,
                                   applicationIdentifier=applicationIdentifier,
                                   instanceIdentifier=instanceIdentifier,
                                   deploymentPlatformEnvironment=deploymentPlatformEnvironment,
                                   buildRoot=buildRoot,
                                   executableFile=executableFile,
                                   applicationDeploymentDirectory=applicationDeploymentDirectory,
                                   configurationTemplateFilepath=configurationTemplateFilepath,
                                   configurationTargetFilepath=configurationTargetFilepath,
                                   files=files,
                                   filesForCleanup=filesForCleanup,
                                   properties=properties,
                                   parent_config=parent_config,
                                   value_pool=value_pool
                                   )

        self.packageIdentifier = packageIdentifier
        self.mainActivity = mainActivity
        self.permissions = permissions
