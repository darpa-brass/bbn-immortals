#
# {
#     "scenarioConfiguration": {
#         "sessionIdentifier" : "B4F3692B0665",
#         "server": {
#             "bandwidth": 1000
#         },
#         "clients": [
#             {
#                 "imageBroadcastIntervalMS": "5000",
#                 "latestSABroadcastIntervalMS": "250",
#                 "count": 2
#             }
#         ]
#
#     },
#     "executionMode": "RUN_SCENARIO",
#     "scenarioIdentifiers" : [
#         "client-test-images"
#     ],
#     "validate": true,
#     "timeout": 60,
#     "keepEnvironmentRunning": false,
#     "wipeExistingEnvironment": true,
#     "displayEmulatorGui": false,
#
#     "wipeExistingEnvironment": false,
# }
import copy

import packages.commentjson as json
from configurationmanager import AndroidApplicationConfig, Configuration, Scenario
from utils import path_helper, value_helper


class ScenarioRunnerConfiguration:
    """
    :type session_identifier: str
    :type deployment_directory: str
    :type scenario: Scenario
    :type validate: bool
    :type timeout: int
    :type setup_environment: bool
    :type setup_applications: bool
    :type execute_scenario: bool
    :type keep_environment_running: bool
    :type wipe_existing_environment: bool
    :type display_emulator_gui: bool
    """

    @classmethod
    def from_scenario_configuration(cls,
                                    scenario_configuration,
                                    scenario_template
                                    ):
        """
        :type scenario_configuration ScenarioConfiguration
        :type scenario_template str
        :rtype ScenarioRunnerConfiguration
        """

        ident = scenario_configuration.session_identifier

        # Construct the scenario runner configuration
        with open(path_helper(True, Configuration.immortals_root, 'harness/scenarioconductor/configs/templates/validation/scenario_runner_configuration.json')) as f:
            f_json = json.load(f)
            scenario_runner_configuration = ScenarioRunnerConfiguration.from_dict(f_json)

        scenario_runner_configuration.session_identifier = scenario_configuration.session_identifier
        scenario_runner_configuration.deployment_directory = \
            path_helper(False, Configuration.immortals_root,
                        value_helper(scenario_runner_configuration.deployment_directory,
                                     scenario_runner_configuration)) + '/'

        scenario_runner_configuration.scenario.parent_config = scenario_runner_configuration

        # Only the server is in there by default for CP1/CP2
        marti_router = scenario_runner_configuration.scenario.deployment_applications[0]  # type: JavaApplicationConfig
        marti_router.parent_config = scenario_runner_configuration.scenario
        marti_router.application_deployment_directory = path_helper(False, Configuration.immortals_root, value_helper(
                marti_router.application_deployment_directory,
                marti_router))

        marti_router.configuration_target_filepath = value_helper(marti_router.configuration_target_filepath,
                                                                  marti_router)

        # Construct the clients and add them to the scenario runner configuration
        with open(path_helper(True, Configuration.immortals_root,'/harness/scenarioconductor/configs/templates/' + scenario_template + '/client_ataklite.json'), 'r') as f:
            client_j = json.load(f)
            client_template = AndroidApplicationConfig.from_dict(client_j, scenario_runner_configuration)

        ccid = 0
        for j in range(len(scenario_configuration.clients)):
            client_configuration = scenario_configuration.clients[j]  # type: ATAKLiteClient

            for i in range(scenario_configuration.clients[j].count):
                i_str = str(i)
                if len(i_str) == 1:
                    i_str = '00' + i_str
                elif len(i_str) == 2:
                    i_str = '0' + i_str

                client = copy.deepcopy(client_template)  # type: AndroidApplicationConfig
                client.parent_config = scenario_runner_configuration
                client.instance_identifier = value_helper(client.instance_identifier, client,
                                                          value_pool={'CCID': str(ccid), 'CID': i_str})
                client.build_root = client.build_root.format(IDENT=ident)
                client.executable_filepath = client.executable_filepath.format(IDENT=ident)

                client.application_deployment_directory = \
                    path_helper(False, Configuration.immortals_root,
                                value_helper(client.application_deployment_directory, client))

                # Add customization properties that will be added to the configuration file at deployment
                client.properties['callsign'] = client.instance_identifier
                client.properties['latestSABroadcastIntervalMS'] = client_configuration.latestsa_broadcast_interval_ms
                client.properties['imageBroadcastIntervalMS'] = client_configuration.image_broadcast_interval_ms

                scenario_runner_configuration.scenario.deployment_applications.append(client)
            ccid += 1

        return scenario_runner_configuration

    @classmethod
    def from_dict(cls, j):
        # type: (dict) -> ScenarioRunnerConfiguration

        scenario = Scenario.from_dict(j['scenario'])
        scenario.parent_config = cls

        return cls(
                j['sessionIdentifier'],
                j['deploymentDirectory'],
                scenario,
                j['validate'],
                j['timeout'],
                j['setupEnvironment'],
                j['setupApplications'],
                j['executeScenario'],
                j['keepEnvironmentRunning'],
                j['wipeExistingEnvironment'],
                j['displayEmulatorGui']
        )

    def __init__(self,
                 session_identifier,  # type: str
                 deployment_directory,  # type: str
                 scenario,  # type: Scenario
                 validate,  # type: bool
                 timeout,  # type: int
                 setup_environment,  # type: bool
                 setup_applications,  # type: bool
                 execute_scenario,  # type: bool
                 keep_environment_running,  # type: bool
                 wipe_existing_environment,  # type: bool
                 display_emulator_gui  # type: bool
                 ):
        self.session_identifier = session_identifier  # type: str
        self.deployment_directory = deployment_directory
        self.scenario = scenario  # type: Scenario
        self.validate = validate  # type: bool
        self.timeout = timeout  # type: int
        self.setup_environment = setup_environment  # type: bool
        self.setup_applications = setup_applications  # type: bool
        self.execute_scenario = execute_scenario  # type: bool
        self.keep_environment_running = keep_environment_running  # type: bool
        self.wipe_existing_environment = wipe_existing_environment  # type: bool
        self.display_emulator_gui = display_emulator_gui  # type: bool

    def clone_and_trim(self):
        clone = copy.deepcopy(self)
        clone.scenario = clone.scenario.clone_and_trim()
        return clone


class MartiServer:
    """
    :type bandwidth int
    """

    @classmethod
    def from_dict(cls, d):
        # type:(dict) -> MartiServer
        return cls(
                d['bandwidth']
        )

    def __init__(self,
                 bandwidth  # type: int
                 ):
        self.bandwidth = bandwidth  # type: int


class ATAKLiteClient:
    """
    :type image_broadcast_interval_ms: int
    :type latestsa_broadcast_interval_ms: int
    :type count: int
    :type present_resources: list[str]
    :type required_properties: list[str]
    """

    @classmethod
    def from_dict(cls, d):
        # type:(dict) -> ATAKLiteClient
        return cls(
                d['imageBroadcastIntervalMS'],
                d['latestSABroadcastIntervalMS'],
                d['count'],
                d['presentResources'],
                d['requiredProperties']
        )

    def __init__(self,
                 image_broadcast_interval_ms,
                 latestsa_broadcast_interval_ms,
                 count,
                 present_resources,
                 required_properties
                 ):
        self.image_broadcast_interval_ms = image_broadcast_interval_ms
        self.latestsa_broadcast_interval_ms = latestsa_broadcast_interval_ms
        self.count = count
        self.present_resources = present_resources
        self.required_properties = required_properties


class ScenarioConfiguration:
    """
    :type session_identifier: str
    :type server: MartiServer
    :type clients: list[ATAKLiteClient]
    """

    def __init__(self,
                 session_identifier,  # type: str
                 server,  # type: MartiServer
                 clients  # type:ATAKLiteClient
                 ):
        self.session_identifier = session_identifier  # type: str
        self.server = server  # type: MartiServer
        self.clients = clients  # type:ATAKLiteClient
        self.server.parent_config = self
        for c in self.clients:
            c.parent_config = self

    @classmethod
    def from_dict(cls, d):
        return cls(
                d['sessionIdentifier'],
                MartiServer.from_dict(d['server']),
                map(lambda c: ATAKLiteClient.from_dict(c), d['clients'])
        )

# def main():
#     with open('sample_configuration.json', 'r') as f:
#         sc_j = json.load(f)
#         sc = ScenarioConfiguration.from_dict(sc_j)
#
#     src = ScenarioRunnerConfiguration.from_scenario_configuration(sc, 'validation')
#     # print json.dumps(src, default=lambda o: o.__dict__, sort_keys=True, indent=4)
#     from scenariorunner import  ScenarioRunner
#     sr = ScenarioRunner(src)
#
#     # sr.execute_scenario()
#
#
# if __name__ == '__main__':
#     main()
