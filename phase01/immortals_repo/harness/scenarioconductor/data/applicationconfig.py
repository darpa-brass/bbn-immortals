from ..utils import path_helper, value_helper
from ..immortalsglobals import IMMORTALS_ROOT


class ApplicationConfig:
    """
    :type application_identifier: str
    :type instance_identifier: str
    :type deployment_platform_environment: str
    :type build_root: str
    :type executable_filepath: str
    :type application_deployment_directory: str
    :type configuration_template_filepath: str
    :type configuration_target_filepath: str
    :type files: dict[str,str]
    :type files_for_cleanup: list[str]
    :type properties: dict[str,str]
    """

    @classmethod
    def from_json(cls, j):
        application = j['application']

        if application == 'marti':
            return JavaApplicationConfig.from_dict(j)

        elif application == 'ataklite':
            return AndroidApplicationConfig.from_dict(j)

        else:
            raise Exception('Unexpected application identifier \'' + application + '\'!')

    def __init__(self,
                 application_identifier,
                 instance_identifier,
                 deployment_platform_environment,
                 build_root,
                 executable_filepath,
                 application_deployment_directory,
                 configuration_template_filepath,
                 configuration_target_filepath,
                 files,
                 files_for_cleanup,
                 properties,
                 parent_config
                 ):

        self.parent_config = parent_config
        abs_files = {}
        for k in files.keys():
            abs_files[path_helper(True, IMMORTALS_ROOT, k)] = files[k]

        self.application_identifier = application_identifier
        self.instance_identifier = instance_identifier
        self.deployment_platform_environment = deployment_platform_environment
        self.build_root = path_helper(True, IMMORTALS_ROOT, value_helper(build_root, self))
        self.executable_filepath = path_helper(True, IMMORTALS_ROOT, value_helper(executable_filepath, self))
        self.application_deployment_directory = application_deployment_directory
        if configuration_template_filepath is None:
            self.configuration_template_filepath = None
        else:
            self.configuration_template_filepath = path_helper(False, IMMORTALS_ROOT, configuration_template_filepath)

        self.configuration_target_filepath = configuration_target_filepath
        self.files = abs_files
        self.files_for_cleanup = files_for_cleanup
        self.properties = properties


class JavaApplicationConfig(ApplicationConfig):
    @classmethod
    def from_dict(cls, j, parent_config=None):
        return cls(
                j['application'],
                j['instanceIdentifier'],
                j['deploymentPlatformEnvironment'],
                j['buildSubdir'],
                j['executableFile'],
                j['applicationDeploymentDirectory'],
                j['configurationTemplateFilepath'],
                j['configurationTargetFilepath'],
                j['files'],
                j['filesForCleanup'],
                j['properties'],
                parent_config
        )

    def __init__(self,
                 application_identifier,  # type: str
                 instance_identifier,  # type: str
                 deployment_platform_environment,  # type: str
                 build_root,  # type: str
                 executable_filepath,  # type: str
                 application_deployment_directory,  # type: str
                 configuration_template_filepath,  # type: str
                 configuration_target_filepath,  # type: str
                 files,  # type: dict
                 files_for_cleanup,
                 properties,  # type: dict
                 parent_config
                 ):
        ApplicationConfig.__init__(self,
                                   application_identifier,
                                   instance_identifier,
                                   deployment_platform_environment,
                                   build_root,
                                   executable_filepath,
                                   application_deployment_directory,
                                   configuration_template_filepath,
                                   configuration_target_filepath,
                                   files,
                                   files_for_cleanup,
                                   properties,
                                   parent_config)

    @classmethod
    def old_init(cls, config, buildroot, files_location=None, copyfrom=None, application_properties=None):
        # These values are set from the configuration if available, or derived from the copyfrom source
        application_identifier = config['application']  # type:str
        build_root = 'buildSubdir' in config and path_helper(True, buildroot,
                                                             config[
                                                                 'buildSubdir']) or copyfrom.build_root  # type:str
        executable_filepath = 'executableFile' in config and path_helper(True, buildroot, config[
            'executableFile']) or copyfrom.executable_filepath  # type:str

        # These value must be set in the config since they are runtime-specific
        # self.instance_identifier = os.environ['USER'] + '-' + config['instanceIdentifier']
        instance_identifier = config['instanceIdentifier']  # type:str
        deployment_platform_environment = config['deploymentPlatformEnvironment']  # type:str

        files = {}  # type: dict

        if 'files' in config:
            config_files = config['files']
            for key in config_files.keys():
                # Make the source paths absolute. Relative paths cause great pain and sorrow....
                files[path_helper(True, files_location, key)] = config_files[key]

        if application_properties is None:
            application_properties = {}

        return cls(
                application_identifier,
                instance_identifier,
                deployment_platform_environment,
                build_root,
                executable_filepath,
                None,
                None,
                None,
                files,
                None,
                application_properties,
                None
        )


class AndroidApplicationConfig(ApplicationConfig):
    """
    :type package_identifier: str
    :type main_activity: str
    :type permissions: list[str]
    """

    @classmethod
    def from_dict(cls, j, parent_config=None):
        """
        :type j dict
        :rtype AndroidApplicationConfig
        """
        return cls(application_identifier=j['application'],
                   instance_identifier=j['instanceIdentifier'],
                   deployment_platform_environment=j['deploymentPlatformEnvironment'],
                   build_root=j['buildRoot'],
                   executable_filepath=j['executableFile'],
                   application_deployment_directory=j['applicationDeploymentDirectory'],
                   configuration_template_filepath=j['configurationTemplateFilepath'],
                   configuration_target_filepath=j['configurationTargetFilepath'],
                   files=j['files'],
                   files_for_cleanup=j['filesForCleanup'],
                   properties=j['properties'],
                   package_identifier=j['packageIdentifier'],
                   main_activity=j['mainActivity'],
                   permissions=j['permissions'],
                   parent_config=parent_config
                   )

    @classmethod
    def old_init(cls, config, buildroot, files_location=None, copyfrom=None):
        pass

    def __init__(self,
                 application_identifier,  # type: str
                 instance_identifier,  # type: str
                 deployment_platform_environment,  # type: str
                 build_root,  # type: str
                 executable_filepath,  # type: str
                 application_deployment_directory,  # type:str
                 configuration_template_filepath,  # type: str
                 configuration_target_filepath,  # type: str
                 files,  # type: dict
                 files_for_cleanup,
                 properties,  # type: dict
                 package_identifier,  # type: str
                 main_activity,  # type: str
                 permissions,  # type: list
                 parent_config=None
                 ):
        ApplicationConfig.__init__(self,
                                   application_identifier,
                                   instance_identifier,
                                   deployment_platform_environment,
                                   build_root,
                                   executable_filepath,
                                   application_deployment_directory,
                                   configuration_template_filepath,
                                   configuration_target_filepath,
                                   files,
                                   files_for_cleanup,
                                   properties,
                                   parent_config)

        self.package_identifier = package_identifier
        self.main_activity = main_activity
        self.permissions = permissions

    @classmethod
    def old_init(cls, config, buildroot, files_location=None, copyfrom=None):
        # These values are set from the configuration if available, or derived from the copyfrom source
        application_identifier = config['application']  # type: str
        build_root = 'buildSubdir' in config and path_helper(True, buildroot,
                                                             config[
                                                                 'buildSubdir']) or copyfrom.build_root  # type: str

        executable_filepath = 'executableFile' in config and path_helper(True, buildroot, config[
            'executableFile']) or copyfrom.executable_filepath  # type: str
        package_identifier = 'packageIdentifier' in config and config[
            'packageIdentifier'] or copyfrom.package_identifier  # type: str
        main_activity = 'mainActivity' in config and config['mainActivity'] or copyfrom.main_activity  # type: str
        permissions = 'permissions' in config and config['permissions'] or list(
                copyfrom.permissions)  # type: list

        # These value must be set in the config since the deployment types defined in the configuration are not valid
        # deployment platforms for actual use
        # self.instance_identifier = os.environ['USER'] + '-' + config['instanceIdentifier']
        instance_identifier = config['instanceIdentifier']  # type: str
        deployment_platform_environment = config['deploymentPlatformEnvironment']  # type: str

        files = {}  # type: dict
        if 'files' in config:
            config_files = config['files']
            for key in config_files.keys():
                # Make the source paths absolute. Relative paths cause great pain and sorrow....
                files[path_helper(True, files_location, key)] = config_files[key]

        params = {}

        return cls(
                application_identifier,
                instance_identifier,
                deployment_platform_environment,
                build_root,
                executable_filepath,
                None,
                None,
                None,
                files,
                None,
                params,
                package_identifier,
                main_activity,
                permissions
        )
