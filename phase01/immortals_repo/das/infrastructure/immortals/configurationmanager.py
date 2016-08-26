import inspect
import os
import sys

PACKAGE_ROOT = os.path.abspath(os.path.dirname(inspect.stack()[0][1]))
DAS_ROOT = os.path.abspath(os.path.join(os.path.dirname(inspect.stack()[0][1]), '../../'))

ANDROID_BIN = 'android'
EMULATOR_BIN = 'emulator'
ADB_BIN = 'adb'
MKSDCARD_BIN = 'mksdcard'

from  packages import commentjson

from utils import path_helper

sys.path.append(os.path.abspath(os.path.dirname(inspect.stack()[0][1])))

try:
    _cfile
except NameError:
    pwd = os.path.abspath(os.path.dirname(inspect.stack()[0][1]))
    _cfile = commentjson.load(open(os.path.join(pwd, 'infrastructure_configuration.json')))
    _immortalization_target_root = path_helper(True, DAS_ROOT, _cfile['immortalizationTarget']['path'])


class DeploymentEnvironment:
    def __init__(self, identifier, config):
        self.identifier = identifier
        self.sdk_level = config['sdkLevel']
        self.deployment_platform = config['deploymentPlatform']


class AndroidApplicationConfig:
    def __init__(self, config, buildroot, files_location=None, copyfrom=None):
        # These values are set from the configuration if available, or derived from the copyfrom source
        self.application_identifier = config['application']
        self.build_root = 'buildSubdir' in config and path_helper(True, buildroot, config['buildSubdir']) or copyfrom.build_root
        self.executable_filepath = 'executableFile' in config and path_helper(True, buildroot, config['executableFile']) or copyfrom.executable_filepath
        self.package_identifier = 'packageIdentifier' in config and config['packageIdentifier'] or copyfrom.package_identifier
        self.main_activity = 'mainActivity' in config and config['mainActivity'] or copyfrom.main_activity
        self.permissions = 'permissions' in config and config['permissions'] or list(copyfrom.permissions)

        # These value must be set in the config since the deployment types defined in the configuration are not valid deployment platforms for actual use
        # self.instance_identifier = os.environ['USER'] + '-' + config['instanceIdentifier']
        self.instance_identifier = config['instanceIdentifier']
        self.deployment_platform_environment = config['deploymentPlatformEnvironment']


        if 'files' in config:
            self.files = {}
            config_files = config['files']
            for key in config_files.keys():
                # Make the source paths absolute. Relative paths cause great pain and sorrow....
                self.files[path_helper(True, files_location, key)] = config_files[key]


class JavaApplicationConfig:
    def __init__(self, config, buildroot, files_location=None, copyfrom=None):
        # These values are set from the configuration if available, or derived from the copyfrom source
        self.application_identifier = config['application']
        self.build_root = 'buildSubdir' in config and path_helper(True, buildroot, config['buildSubdir']) or copyfrom.build_root
        self.executable_filepath = 'executableFile' in config and path_helper(True, buildroot, config['executableFile']) or copyfrom.executable_filepath

        # These value must be set in the config since they are runtime-specific
        # self.instance_identifier = os.environ['USER'] + '-' + config['instanceIdentifier']
        self.instance_identifier = config['instanceIdentifier']
        self.deployment_platform_environment = config['deploymentPlatformEnvironment']

        if 'files' in config:
            self.files = {}
            config_files = config['files']
            for key in config_files.keys():
                # Make the source paths absolute. Relative paths cause great pain and sorrow....
                self.files[path_helper(True, files_location, key)] = config_files[key]


_deployment_platform_config_mappings = {"android" : AndroidApplicationConfig, "java" : JavaApplicationConfig}

class Configuration:

    display_ui = True
    runtime_rootpath = path_helper(False, DAS_ROOT, _cfile['runtimeRoot'])
    target_source_rootpath = _immortalization_target_root
    scenarios = {}

    deployment_environments = {}
    for _platform_identifier in _cfile['deploymentEnvironments'].keys():
        _platform_config = _cfile['deploymentEnvironments'][_platform_identifier]
        deployment_environments[_platform_identifier] = DeploymentEnvironment(_platform_identifier, _platform_config)

    deployment_applications = {}
    for _target_config in _cfile['deploymentApplications']:
        deployment_applications[_target_config['application']] = _deployment_platform_config_mappings[_target_config['deploymentPlatformEnvironment']](_target_config, _immortalization_target_root)

    class scenario_runner:
        _s = _cfile['scenarioRunner']
        configuration_filepath = path_helper(True, PACKAGE_ROOT, _s['configurationFile'])

        class docker:
            _d = _cfile['scenarioRunner']['docker']
            scripts = _d['scripts']

    class fuseki:
        _f = _cfile['fuseki']
        rootpath = path_helper(True, DAS_ROOT, _f['root'])
        port = _f['port']


    class repository_service:
        _r = _cfile['repositoryService']

        rootpath = path_helper(True, DAS_ROOT, _r['root'])
        executable_filepath = path_helper(True, DAS_ROOT, _r['executableFile'])
        port = _r['port']


    class das_service:
        _d = _cfile['dasService']

        rootpath = path_helper(True, DAS_ROOT, _d['root'])
        executable_filepath = path_helper(True, DAS_ROOT, _d['executableFile'])
        port = _d['port']



"""
Defines scenarios to run
"""
class Scenario:

    def __init__(self, scenario_identifier, files_location, scenario_config):
        self.deployment_applications = []
        self.scenario_identifier = scenario_identifier
        self.duration_seconds = scenario_config['durationSeconds']

        for scenario_application_config in scenario_config['deploymentApplications']:
            application_identifier = scenario_application_config['application']

            deployment_environment = scenario_application_config['deploymentPlatformEnvironment']
            deployment_platform = Configuration.deployment_environments[deployment_environment].deployment_platform

            scenario_target = _deployment_platform_config_mappings[deployment_platform](scenario_application_config, _immortalization_target_root, files_location, Configuration.deployment_applications[application_identifier])
            self.deployment_applications.append(scenario_target)

with open(Configuration.scenario_runner.configuration_filepath) as _scenario_file:
    _scenario_config = commentjson.load(_scenario_file)

    _files_location =  path_helper(True, os.path.dirname(Configuration.scenario_runner.configuration_filepath), _scenario_config['fileDirectory'])

    for scenario_identifier in _scenario_config['scenarios'].keys():
        scenario_config = _scenario_config['scenarios'][scenario_identifier]

        Configuration.scenarios[scenario_identifier] = Scenario(scenario_identifier, _files_location, scenario_config)
