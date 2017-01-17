import copy
import logging

from .applicationconfig import AndroidApplicationConfig
from .base.tools import path_helper, fillout_object, dictify
from .. import immortalsglobals as ig
from ..data.scenarioconfiguration import ScenarioConfiguration
from ..packages import commentjson as json


# noinspection PyPep8Naming
class Lifecycle:
    """
    :type setupEnvironment: bool
    :type setupApplications: bool
    :type executeScenario: bool
    :type haltEnvironment: bool
    """

    @classmethod
    def from_dict(cls, d, parent_config):
        return cls(
            setupEnvironment=d['setupEnvironment'],
            setupApplications=d['setupApplications'],
            executeScenario=d['executeScenario'],
            haltEnvironment=d['haltEnvironment'],
            parent_config=parent_config
        )

    def __init__(self,
                 setupEnvironment,
                 setupApplications,
                 executeScenario,
                 haltEnvironment,
                 parent_config
                 ):
        self.setupEnvironment = setupEnvironment
        self.setupApplications = setupApplications
        self.executeScenario = executeScenario
        self.haltEnvironment = haltEnvironment
        self.parent_config = parent_config

    def to_dict(self):
        return {
            'setupEnvironment': self.setupEnvironment,
            'setupApplications': self.setupApplications,
            'executeScenario': self.executeScenario,
            'haltEnvironment': self.haltEnvironment
        }


# noinspection PyPep8Naming
class SetupEnvironmentLifecycle:
    """
    :type destroyExisting: bool
    :type cleanExisting: bool
    """

    @classmethod
    def from_dict(cls, d, parent_config):
        return cls(
            destroyExisting=d['destroyExisting'],
            cleanExisting=d['cleanExisting'],
            parent_config=parent_config
        )

    def __init__(self, destroyExisting, cleanExisting, parent_config):
        self.destroyExisting = destroyExisting
        self.cleanExisting = cleanExisting
        self.parent_config = parent_config

    def to_dict(self):
        return {
            'destroyExisting': self.destroyExisting,
            'cleanExisting': self.cleanExisting
        }


# noinspection PyPep8Naming
class ScenarioRunnerConfiguration:
    """
    :type sessionIdentifier: str
    :type deploymentDirectory: str
    :type scenario: ScenarioConductorConfiguration
    :type validate: bool
    :type startEmulatorsSimultaneously: bool
    :type lifecycle: Lifecycle
    :type setupEnvironmentLifecycle: SetupEnvironmentLifecycle
    :type swallowAndShutdownOnException: bool
   """

    @classmethod
    def from_scenario_api_configuration(cls,
                                        scenario_configuration,
                                        scenario_template,
                                        swallowAndShutdownOnException
                                        ):
        """
        :type scenario_configuration ScenarioConductorConfiguration
        :type scenario_template str
        :rtype ScenarioRunnerConfiguration
        :type swallowAndShutdownOnException bool
        """

        config_root = 'harness/scenarioconductor/configs/templates/' + scenario_template + '/'

        # Construct the scenario runner configuration
        with open(path_helper(True, ig.IMMORTALS_ROOT,
                              config_root + 'scenario_runner_configuration.json')) as f:
            f_json = json.load(f)
            scenario_runner_configuration = ScenarioRunnerConfiguration.from_dict(
                j=f_json,
                parent_config=None,
                value_pool={
                    'sessionIdentifier': scenario_configuration.sessionIdentifier,
                    'runtimeRoot': ig.configuration.runtimeRoot
                })
            scenario_runner_configuration.swallowAndShutdownOnException = swallowAndShutdownOnException

        scenario_runner_configuration.deploymentDirectory = \
            path_helper(False, ig.IMMORTALS_ROOT, scenario_runner_configuration.deploymentDirectory)

        scenario_runner_configuration.scenario.parent_config = scenario_runner_configuration
        scenario_runner_configuration.minDurationMS = ig.configuration.validation.minimumTestDurationMS

        # Only the server is in there by default for CP1/CP2
        marti_router = scenario_runner_configuration.scenario.deploymentApplications[0]  # type: JavaApplicationConfig
        marti_router.parent_config = scenario_runner_configuration.scenario
        marti_router.applicationDeploymentDirectory = path_helper(False, ig.IMMORTALS_ROOT,
                                                                  marti_router.applicationDeploymentDirectory)

        # Construct the clients and add them to the scenario runner configuration
        with open(path_helper(True, ig.IMMORTALS_ROOT,
                              'harness/scenarioconductor/configs/templates/' + scenario_template + '/client_ataklite.json'),
                  'r') as f:
            client_j = json.load(f)

        ccid = 0
        for j in range(len(scenario_configuration.clients)):
            client_configuration = scenario_configuration.clients[j]  # type: ATAKLiteClient

            for i in range(scenario_configuration.clients[j].count):
                i_str = str(i)
                if len(i_str) == 1:
                    i_str = '00' + i_str
                elif len(i_str) == 2:
                    i_str = '0' + i_str

                client = AndroidApplicationConfig.from_dict(
                    d=copy.deepcopy(client_j),
                    parent_config=scenario_runner_configuration,
                    value_pool={'CCID': str(ccid),
                                'CID': i_str,
                                'sessionIdentifier': scenario_configuration.sessionIdentifier,
                                'runtimeRoot': ig.configuration.runtimeRoot}
                )

                logging.error("CID: " + client.instanceIdentifier)

                for key in client.files.keys():
                    value = client.files[key]

                    if value == '/sdcard/ataklite/ATAKLite-Config.json':
                        if value not in client.configurationCustomizations:
                            client.configurationCustomizations[key] = {}

                        client.configurationCustomizations[key]['callsign'] = client.instanceIdentifier
                        client.configurationCustomizations[key][
                            'latestSABroadcastIntervalMS'] = client_configuration.latestSABroadcastIntervalMS
                        client.configurationCustomizations[key][
                            'imageBroadcastIntervalMS'] = client_configuration.imageBroadcastIntervalMS

                    elif value == '/sdcard/ataklite/env.json':
                        if value not in client.configurationCustomizations:
                            client.configurationCustomizations[key] = {}

                        client.configurationCustomizations[key][
                            'availableResources'] = client_configuration.presentResources

                scenario_runner_configuration.scenario.deploymentApplications.append(client)
            ccid += 1

        return scenario_runner_configuration

    @classmethod
    def from_dict(cls, j, parent_config, value_pool={}):
        r = cls(
            sessionIdentifier=j['sessionIdentifier'],
            deploymentDirectory=j['deploymentDirectory'],
            scenario=None,
            validate=j['validate'],
            startEmulatorsSimultaneously=j['startEmulatorsSimultaneously'],
            lifecycle=None,
            setupEnvironmentLifecycle=None,
            minDurationMS=j['minDurationMS'],
            debugMode=j['debugMode'],
            swallowAndShutdownOnException=j['swallowAndShutdownOnException'],
            parent_config=parent_config
        )
        fillout_object(r, value_pool=value_pool)
        r.deploymentDirectory = path_helper(False, ig.IMMORTALS_ROOT, r.deploymentDirectory) + '/'
        r.scenario = ScenarioConfiguration.from_dict(j=j['scenario'], parent_config=r, value_pool=value_pool)
        fillout_object(r.scenario)
        r.lifecycle = Lifecycle.from_dict(j['lifecycle'], r)
        r.setupEnvironmentLifecycle = SetupEnvironmentLifecycle.from_dict(j['setupEnvironmentLifecycle'], r)
        fillout_object(r)
        return r

    def __init__(self,
                 sessionIdentifier,
                 deploymentDirectory,
                 scenario,
                 validate,
                 startEmulatorsSimultaneously,
                 lifecycle,
                 setupEnvironmentLifecycle,
                 minDurationMS,
                 debugMode,
                 swallowAndShutdownOnException,
                 parent_config
                 ):
        self.sessionIdentifier = sessionIdentifier
        self.deploymentDirectory = deploymentDirectory
        self.scenario = scenario
        self.validate = validate
        self.startEmulatorsSimultaneously = startEmulatorsSimultaneously
        self.lifecycle = lifecycle
        self.setupEnvironmentLifecycle = setupEnvironmentLifecycle
        self.minDurationMS = minDurationMS
        self.debugMode = debugMode
        self.swallowAndShutdownOnException = swallowAndShutdownOnException
        self.parent_config = parent_config

        d = dictify(self)
        print 'AAA'
        print json.dumps(d)
        # print utils.dictify(self)
        print 'BBB'

    def clone_and_trim(self):
        clone = copy.deepcopy(self)
        clone.scenario = clone.scenario.clone_and_trim()
        return clone
