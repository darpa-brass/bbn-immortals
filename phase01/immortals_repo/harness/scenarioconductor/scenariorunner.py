#!/usr/bin/env python

import argparse
import atexit
import logging
import os
import shutil
import signal
import sys
import time
from threading import Thread

import platformhelper
from behaviorvalidator import BehaviorValidator
from configurationmanager import Configuration, path_helper
from data.scenarioconfiguration import ScenarioConfiguration
from data.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from immortalsglobals import ImmortalsGlobals


def _exit_handler():
    pass


atexit.register(_exit_handler)

logging.basicConfig(level=logging.WARN)

parser = argparse.ArgumentParser(description='IMMoRTALS Scenario Runner')
argumentCommandGroup = parser.add_mutually_exclusive_group()
argumentCommandGroup.add_argument('-l', '--list-scenarios', action='store_true', help='List available scenarios')
argumentCommandGroup.add_argument('-r', '--run-scenario', type=str, metavar='SCENARIO_IDENTIFIER',
                                  help='Restart the adb server and run the default test scenario')
argumentCommandGroup.add_argument('-s', '--scenario-setup', type=str, metavar='SCENARIO_IDENTIFIER',
                                  help='Set up the execution environment. This involves everything up to and including copying and installing the applications that are paret of the scenario')
argumentCommandGroup.add_argument('-x', '--scenario-execute', type=str, metavar='SCENARIO_IDENTIFIER',
                                  help='Start the run exection. If the scenario has not been set up, this could cause unexpected behavior!')
parser.add_argument('-g', '--gui', action='store_true',
                    help='Display the UI for execution environments where one is available.')
parser.add_argument('-w', '--wipe-existing-environment', action='store_true',
                    help='Will wipe any existing environment in its entirety and set everything up from scratch')
parser.add_argument('-k', '--keep-constructed-environment-running', action='store_true',
                    help='Will keep the environment running after execution is finished (primarily used for debugging')
parser.add_argument('-v', '--perform-validation', action='store_true',
                    help='Will execute validation of the scenario(s) specified to be run')
parser.add_argument('-t', '--timeout', type=int,
                    help='The timeout at which the scenario will end, regardless of validation completion.')
parser.add_argument('-c', '--configuration', type=str, help='A JSON-formatted base64-encoded configuration file')
parser.add_argument('-f', '--file', type=str,
                    help='Loads all the configuration data from the specified file, ignoring any other parameters')


def _exit_handler(signal, frame):
    for file_object in _file_objects:
        if file_object is not None and not file_object.closed:
            file_object.flush()
            file_object.close()


signal.signal(signal.SIGINT, _exit_handler)
_file_objects = []


class ScenarioRunner:
    """
    :type runner_configuration: ScenarioRunnerConfiguration
    :type scenario_configuration: ScenarioConfiguration
    """

    def __init__(self,
                 runner_configuration,  # type: data_skeletons.ScenarioRunnerConfiguration
                 scenario_configuration
                 ):
        self.runner_configuration = runner_configuration
        self.scenario_configuration = scenario_configuration
        ImmortalsGlobals.wipe_existing_environment = runner_configuration.wipe_existing_environment
        ImmortalsGlobals.keep_constructed_environment_running = runner_configuration.keep_environment_running
        ImmortalsGlobals.display_guis = runner_configuration.display_emulator_gui

        self.scenario_root = path_helper(False, Configuration.immortals_root, runner_configuration.deployment_directory)
        self.application_instances = []  # type:list
        self.timer = None
        self.timeout = runner_configuration.timeout is not None and runner_configuration.timeout or runner_configuration.scenario.duration_seconds
        self.behaviorvalidator = None

    def execute_scenario(self):
        if ImmortalsGlobals.wipe_existing_environment:
            self.wipe_environment()

        if self.runner_configuration.setup_environment:
            self.setup_environment()

        if self.runner_configuration.setup_applications:
            self.setup_applications()

        if self.runner_configuration.execute_scenario:
            self.start_applications()

        if self.runner_configuration.scenario.validator_identifiers is None or not self.runner_configuration.validate:
            if self.timeout is not None and self.timeout > 0:
                time.sleep(self.timeout)
                self.halt_scenario()

    def setup_environment(self):
        if os.path.exists(self.scenario_root):
            raise Exception('Attempted to run "setup_environment" over an existing environment!')

        # Make directories
        os.makedirs(self.scenario_root)

        self.runner_configuration.deployment_directory

        for application in self.runner_configuration.scenario.deployment_applications:
            os.mkdir(os.path.join(self.scenario_root, application.instance_identifier))

    """
    Remove the test environment folder if it exists
    """

    def wipe_environment(self):
        if os.path.exists(self.scenario_root):
            shutil.rmtree(self.scenario_root)

    def setup_applications(self):
        if len(self.application_instances) == 0:
            self._init_applications()

        if self.runner_configuration.start_emulators_simultaneously:

            if self.runner_configuration.setup_environment:
                thread_pool = []
                for application in self.application_instances:
                    t = Thread(target=application.setup_start_environment)
                    thread_pool.append(t)
                    t.start()

                for t in thread_pool:  # type: Thread
                    t.join()

            if self.runner_configuration.setup_applications:
                for application in self.application_instances:
                    application.setup_application()

        else:
            for application in self.application_instances:
                if self.runner_configuration.setup_environment:
                    application.setup_start_environment()

                if self.runner_configuration.setup_applications:
                    application.setup_application()

    def start_applications(self):
        if len(self.application_instances) == 0:
            self._init_applications()

        if self.runner_configuration.scenario.validator_identifiers is not None and self.runner_configuration.validate:
            client_identifiers = []

            for client in self.runner_configuration.scenario.deployment_applications:  # type: ApplicationConfig
                if client.application_identifier == 'ATAKLite':
                    client_identifiers.append(client.instance_identifier)

            self.behaviorvalidator = BehaviorValidator(self.runner_configuration)
            self.behaviorvalidator.start(self.halt_scenario)

        for application in self.application_instances:
            with open(os.path.join(application.config.application_deployment_directory, 'log.txt'), 'w') as log:
                _file_objects.append(log)

                application.start_application()

    def halt_scenario(self, results=None):
        if self.timer is not None:
            self.timer.cancel()

        # Stop all clients
        for application in self.application_instances:
            application.stop_application()

        if not ImmortalsGlobals.keep_constructed_environment_running:
            for application in self.application_instances:
                application.stop_environment()

        if self.behaviorvalidator is not None:
            self.behaviorvalidator.stop()

        sys.exit(0)

    def _init_applications(self):
        deployment_applications = self.runner_configuration.scenario.deployment_applications

        for configuration_instance in deployment_applications:
            application_instance = platformhelper.create_application_instance(configuration_instance)
            self.application_instances.append(application_instance)

# def main():
#     args = parser.parse_args()
#
#     if args.file:
#         with open(args.file, 'r') as f:
#             json_dict = json.load(f)
#
#         scenario_runner_config = ScenarioConverter.produce_scenario(json_dict)
#
#     elif args.list_scenarios:
#         for test in config.scenarios.keys():
#             print test
#     else:
#         scenario_runner_config = ScenarioRunnerConfiguration(
#                 ScenarioConfiguration(
#                         str(uuid.uuid4())[-12:],
#                         MartiServer(10000000),
#
#                 )
#         )
#
#     if args.run_scenario:
#         scenario_identifier = args.run_scenario
#     elif args.scenario_setup:
#         scenario_identifier = args.scenario_setup
#     elif args.scenario_execute:
#         scenario_identifier = args.scenario_execute
#
#     if args.wipe_existing_environment:
#         ImmortalsGlobals.wipe_existing_environment = args.wipe_existing_environment
#
#     if args.keep_constructed_environment_running:
#         ImmortalsGlobals.keep_constructed_environment_running = args.keep_constructed_environment_running
#
#     # do_wipe_environments = args.force_setup_environment
#     do_application_setup = args.run_scenario or args.scenario_setup
#     do_environment_setup = args.run_scenario or args.scenario_setup
#     do_execute_scenario = args.run_scenario or args.scenario_execute
#     do_display_ui = args.gui
#
#     if args.list_scenarios:
#         for test in config.scenarios.keys():
#             print test
#
#     elif args.run_scenario or args.scenario_setup or args.scenario_execute:
#         if scenario_identifier not in config.scenarios:
#             print 'The test identifier "' + scenario_identifier + '" is not a valid test. Valid tests:'
#             for scenario in config.scenarios.keys():
#                 print scenario
#         else:
#
#             runner = ScenarioRunner(scenario_identifier, do_environment_setup, do_application_setup,
#                                     do_execute_scenario, do_display_ui, args.timeout, args.perform_validation)
#             runner.execute_scenario()
#
#     else:
#         parser.print_help()
#
#
