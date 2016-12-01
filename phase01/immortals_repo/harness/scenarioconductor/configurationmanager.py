import copy
import inspect
import os
import sys

from data.applicationconfig import AndroidApplicationConfig, ApplicationConfig, JavaApplicationConfig
from data.deploymentenvironment import DeploymentEnvironment
from immortalsglobals import IMMORTALS_ROOT
from packages import commentjson
from utils import path_helper

PACKAGE_ROOT = os.path.abspath(os.path.dirname(inspect.stack()[0][1]))

ANDROID_BIN = 'android'
EMULATOR_BIN = 'emulator'
ADB_BIN = 'adb'
MKSDCARD_BIN = 'mksdcard'

# TODO: this can probably be changed since the singular issue has been solved...
try:
    # noinspection PyUnboundLocalVariable
    _cfile
except NameError:
    pwd = os.path.abspath(os.path.dirname(inspect.stack()[0][1]))
    _cfile = commentjson.load(open(os.path.join(pwd, 'infrastructure_configuration.json')))
    _immortalization_target_root = path_helper(True, IMMORTALS_ROOT, _cfile['immortalizationTarget']['path'])

_deployment_platform_config_mappings = {"android": AndroidApplicationConfig, "java": JavaApplicationConfig}


class Configuration:
    """
    :type display_ui: bool
    :type runtime_rootpath: str
    :type result_rootpath: str
    :type target_source_rootpath: str
    :type scenarios: dict[str,Scenario]
    :type deployment_environments: dict[str, data.deploymentenvironment.DeploymentEnvironment]
    :type deployment_applications: dict[str, data.applicationconfig.ApplicationConfig]
    """

    def __init__(self):
        pass

    immortals_root = IMMORTALS_ROOT
    display_ui = True  # type: bool
    runtime_rootpath = path_helper(False, IMMORTALS_ROOT, _cfile['runtimeRootpath'])  # type:str
    result_rootpath = path_helper(False, IMMORTALS_ROOT, _cfile['resultRootpath'])  # type:str
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
        rootpath = path_helper(True, IMMORTALS_ROOT, _f['root'])  # type:str
        port = _f['port']  # type:int

    class repository_service:
        _r = _cfile['repositoryService']

        rootpath = path_helper(True, IMMORTALS_ROOT, _r['root'])  # type:str
        executable_filepath = path_helper(True, IMMORTALS_ROOT, _r['executableFile'])  # type:str
        port = _r['port']  # type:int

    class das_service:
        _d = _cfile['dasService']

        rootpath = path_helper(True, IMMORTALS_ROOT, _d['root'])  # type:str
        executable_filepath = path_helper(True, IMMORTALS_ROOT, _d['executableFile'])  # type:str
        port = _d['port']  # type:int

    class validation_program:
        _v = _cfile['validationProgram']
        rootpath = path_helper(True, IMMORTALS_ROOT, _v['root'])  # type:str
        executable_filepath = path_helper(True, IMMORTALS_ROOT, _v['executableFile'])  # type:str
        basic_parameters = _v['basicParameters']  # type: list


"""
Defines scenarios to run
"""

# noinspection PyPep8Naming
class Scenario:
    """
    :type scenario_identifier: str
    :type durationMS: int
    :type deployment_applications: list[data.applicationconfig.ApplicationConfig]
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
                j['durationMS'],
                map(lambda app: ApplicationConfig.from_json(app), j['deploymentApplications']),
                j['validatorIdentifiers']
        )

    def __init__(self,
                 scenario_identifier,  # type: str
                 durationMS,  # type: int
                 deployment_applications,  # type: list
                 validator_identifiers  # type: list
                 ):
        self.scenario_identifier = scenario_identifier  # type:str
        self.durationMS = durationMS  # type:int
        self.deployment_applications = deployment_applications  # type: list
        self.validator_identifiers = validator_identifiers  # type:list
        self.parent_config = None

    # @classmethod
    # def from_old_init(cls, scenario_identifier, files_location, scen_config):
    #     deployment_applications = []  # type: list
    #     scenario_identifier = scenario_identifier  # type: str
    #     durationMS = scen_config['durationMS']  # type: str
    #     if 'validatorIdentifiers' in scen_config:
    #         validator_identifiers = scen_config['validatorIdentifiers']  # type: list
    #     else:
    #         validator_identifiers = []  # type: list(str)
    #
    #     for scenario_application_config in scen_config['deploymentApplications']:
    #         application_identifier = scenario_application_config['application']
    #
    #         deployment_environment = scenario_application_config['deploymentPlatformEnvironment']
    #         deployment_platform = Configuration.deployment_environments[deployment_environment].deployment_platform
    #
    #         scenario_target = _deployment_platform_config_mappings[deployment_platform].old_init(
    #                 scenario_application_config,
    #                 _immortalization_target_root,
    #                 files_location,
    #                 Configuration.deployment_applications[application_identifier]
    #         )
    #         deployment_applications.append(scenario_target)
    #
    #     return cls(
    #             scenario_identifier,
    #             durationMS,
    #             deployment_applications,
    #             validator_identifiers
    #     )
    #
    # def clone_and_trim(self):
    #     clone = copy.deepcopy(self)
    #     clone.parent_config = None
    #
    #     for application in clone.deployment_applications:
    #         application.parent_config = None
    #
    #     return clone


with open(Configuration.scenario_runner.configuration_filepath) as _scenario_file:
    _scenario_config = commentjson.load(_scenario_file)

    _files_location = path_helper(True, os.path.dirname(Configuration.scenario_runner.configuration_filepath),
                                  _scenario_config['fileDirectory'])
    _files_location = IMMORTALS_ROOT

    # for scen_ident in _scenario_config['scenarios'].keys():
    #     scenario_config = _scenario_config['scenarios'][scen_ident]
    #
    #     Configuration.scenarios[scen_ident] = Scenario.from_old_init(scen_ident, _files_location,
    #                                                                  scenario_config)
