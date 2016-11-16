import copy
import inspect
import os
import sys

from packages import commentjson
from utils import path_helper, value_helper

PACKAGE_ROOT = os.path.abspath(os.path.dirname(inspect.stack()[0][1]))

DAS_ROOT = os.path.abspath(os.path.join(os.path.dirname(inspect.stack()[0][1]), '../../'))

ANDROID_BIN = 'android'
EMULATOR_BIN = 'emulator'
ADB_BIN = 'adb'
MKSDCARD_BIN = 'mksdcard'

sys.path.append(os.path.abspath(os.path.dirname(inspect.stack()[0][1])))

try:
    # noinspection PyUnboundLocalVariable
    _cfile
except NameError:
    pwd = os.path.abspath(os.path.dirname(inspect.stack()[0][1]))
    _cfile = commentjson.load(open(os.path.join(pwd, 'infrastructure_configuration.json')))
    _immortalization_target_root = path_helper(True, DAS_ROOT, _cfile['immortalizationTarget']['path'])


class DeploymentEnvironment:
    """
    :type identifier: str
    :type: sdk_level: str
    :type deployment_platform: str
    """

    @classmethod
    def from_dict(cls, d):
        return cls(
                d['identifier'],
                d['sdkLevel'],
                d['deploymentPlatform']
        )

    def __init__(self, identifier, config):
        self.identifier = identifier
        self.sdk_level = config['sdkLevel']
        self.deployment_platform = config['deploymentPlatform']


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
                 properties,
                 parent_config
                 ):

        self.parent_config = parent_config
        abs_files = {}
        for k in files.keys():
            abs_files[path_helper(True, DAS_ROOT, k)] = files[k]

        self.application_identifier = application_identifier
        self.instance_identifier = instance_identifier
        self.deployment_platform_environment = deployment_platform_environment
        self.build_root = path_helper(True, DAS_ROOT, value_helper(build_root, self))
        self.executable_filepath = path_helper(True, DAS_ROOT, value_helper(executable_filepath, self))
        self.application_deployment_directory = application_deployment_directory
        if configuration_template_filepath is None:
            self.configuration_template_filepath = None
        else:
            self.configuration_template_filepath = path_helper(False, DAS_ROOT, configuration_template_filepath)

        self.configuration_target_filepath = configuration_target_filepath
        self.files = abs_files
        self.properties = properties


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
        return cls(j['application'],
                   j['instanceIdentifier'],
                   j['deploymentPlatformEnvironment'],
                   j['buildRoot'],
                   j['executableFile'],
                   j['applicationDeploymentDirectory'],
                   j['configurationTemplateFilepath'],
                   j['configurationTargetFilepath'],
                   j['files'],
                   j['properties'],
                   j['packageIdentifier'],
                   j['mainActivity'],
                   j['permissions'],
                   parent_config
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
                 application_deployment_directory, #  type:str
                 configuration_template_filepath,  # type: str
                 configuration_target_filepath,  # type: str
                 files,  # type: dict
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
                params,
                package_identifier,
                main_activity,
                permissions
        )


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
                j['properties'],
                parent_config
        )

    def __init__(self,
                 application_identifier,  # type: str
                 instance_identifier,  # type: str
                 deployment_platform_environment,  # type: str
                 build_root,  # type: str
                 executable_filepath,  # type: str
                 application_deployment_directory,  #type: str
                 configuration_template_filepath,  # type: str
                 configuration_target_filepath,  # type: str
                 files,  # type: dict
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
                application_properties,
                None
        )


_deployment_platform_config_mappings = {"android": AndroidApplicationConfig, "java": JavaApplicationConfig}


class Configuration:
    """
    :type display_ui: bool
    :type runtime_rootpath: str
    :type result_rootpath: str
    :type target_source_rootpath: str
    :type scenarios: dict[str,Scenario]
    :type deployment_environments: dict[str, DeploymentEnvironment]
    :type deployment_applications: dict[str, ApplicationConfig]
    """

    def __init__(self):
        pass

    immortals_root = DAS_ROOT
    display_ui = True  # type: bool
    runtime_rootpath = path_helper(False, DAS_ROOT, _cfile['runtimeRootpath'])  # type:str
    result_rootpath = path_helper(False, DAS_ROOT, _cfile['resultRootpath'])  # type:str
    target_source_rootpath = _immortalization_target_root  # type:str
    scenarios = {}  # type: dict

    deployment_environments = {}  # type: dict
    for _platform_identifier in _cfile['deploymentEnvironments'].keys():
        _platform_config = _cfile['deploymentEnvironments'][_platform_identifier]
        deployment_environments[_platform_identifier] = DeploymentEnvironment(_platform_identifier,
                                                                              _platform_config)

    deployment_applications = {}  # type: dict
    for _target_config in _cfile['deploymentApplications']:
        deployment_applications[_target_config['application']] = _deployment_platform_config_mappings[
            _target_config['deploymentPlatformEnvironment']].old_init(_target_config, _immortalization_target_root)

    class scenario_runner:
        _s = _cfile['scenarioRunner']
        configuration_filepath = path_helper(True, PACKAGE_ROOT, _s['configurationFile'])  # type:str

        class docker:
            _d = _cfile['scenarioRunner']['docker']
            scripts = _d['scripts']  # type list[str]

    class fuseki:
        _f = _cfile['fuseki']
        rootpath = path_helper(True, DAS_ROOT, _f['root'])  # type:str
        port = _f['port']  # type:int

    class repository_service:
        _r = _cfile['repositoryService']

        rootpath = path_helper(True, DAS_ROOT, _r['root'])  # type:str
        executable_filepath = path_helper(True, DAS_ROOT, _r['executableFile'])  # type:str
        port = _r['port']  # type:int

    class das_service:
        _d = _cfile['dasService']

        rootpath = path_helper(True, DAS_ROOT, _d['root'])  # type:str
        executable_filepath = path_helper(True, DAS_ROOT, _d['executableFile'])  # type:str
        port = _d['port']  # type:int

    class validation_program:
        _v = _cfile['validationProgram']
        rootpath = path_helper(True, DAS_ROOT, _v['root'])  # type:str
        executable_filepath = path_helper(True, DAS_ROOT, _v['executableFile'])  # type:str
        basic_parameters = _v['basicParameters']  # type: list


"""
Defines scenarios to run
"""


class Scenario:
    """
    :type scenario_identifier: str
    :type duration_seconds: int
    :type deployment_applications: list[ApplicationConfig]
    :type validator_identifiers: list[str]
    """

    @classmethod
    def from_dict(cls, j):
        """
        :type j dict
        :rtype Scenario
        """
        return cls(
                j['scenarioIdentifier'],
                j['durationSeconds'],
                map(lambda app: ApplicationConfig.from_json(app), j['deploymentApplications']),
                j['validatorIdentifiers']
        )

    def __init__(self,
                 scenario_identifier,  # type: str
                 duration_seconds,  # type: int
                 deployment_applications,  # type: list
                 validator_identifiers  # type: list
                 ):
        self.scenario_identifier = scenario_identifier  # type:str
        self.duration_seconds = duration_seconds  # type:int
        self.deployment_applications = deployment_applications  # type: list
        self.validator_identifiers = validator_identifiers  # type:list
        self.parent_config = None

    @classmethod
    def from_old_init(cls, scenario_identifier, files_location, scen_config):
        deployment_applications = []  # type: list
        scenario_identifier = scenario_identifier  # type: str
        duration_seconds = scen_config['durationSeconds']  # type: str
        if 'validatorIdentifiers' in scen_config:
            validator_identifiers = scen_config['validatorIdentifiers']  # type: list
        else:
            validator_identifiers = []  # type: list(str)

        for scenario_application_config in scen_config['deploymentApplications']:
            application_identifier = scenario_application_config['application']

            deployment_environment = scenario_application_config['deploymentPlatformEnvironment']
            deployment_platform = Configuration.deployment_environments[deployment_environment].deployment_platform

            scenario_target = _deployment_platform_config_mappings[deployment_platform].old_init(
                    scenario_application_config,
                    _immortalization_target_root,
                    files_location,
                    Configuration.deployment_applications[application_identifier]
            )
            deployment_applications.append(scenario_target)

        return cls(
                scenario_identifier,
                duration_seconds,
                deployment_applications,
                validator_identifiers
        )

    def clone_and_trim(self):
        clone = copy.deepcopy(self)
        clone.parent_config = None

        for application in clone.deployment_applications:
            application.parent_config = None

        return clone


with open(Configuration.scenario_runner.configuration_filepath) as _scenario_file:
    _scenario_config = commentjson.load(_scenario_file)

    _files_location = path_helper(True, os.path.dirname(Configuration.scenario_runner.configuration_filepath),
                                  _scenario_config['fileDirectory'])
    _files_location = DAS_ROOT

    for scen_ident in _scenario_config['scenarios'].keys():
        scenario_config = _scenario_config['scenarios'][scen_ident]

        Configuration.scenarios[scen_ident] = Scenario.from_old_init(scen_ident, _files_location,
                                                                     scenario_config)
