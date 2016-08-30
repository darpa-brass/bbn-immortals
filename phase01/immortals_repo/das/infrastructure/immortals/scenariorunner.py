#!/usr/bin/env python

import sys


import argparse
import atexit
import logging
import os
import shutil
import signal
import time

from packages import commentjson

from utils import path_helper

from configurationmanager import Configuration as config
from immortalsglobals import ImmortalsGlobals
# from behaviorvalidator import BehaviorValidator
import platformhelper



def _exit_handler():
    pass

atexit.register(_exit_handler)

logging.basicConfig(level=logging.DEBUG)

parser = argparse.ArgumentParser(description='IMMoRTALS Scenario Runner')
argumentCommandGroup = parser.add_mutually_exclusive_group()
argumentCommandGroup.add_argument('-l', '--list-scenarios', action='store_true', help='List available scenarios')
argumentCommandGroup.add_argument('-r', '--run-scenario', type=str, metavar='SCENARIO_IDENTIFIER', help='Restart the adb server and run the default test scenario')
argumentCommandGroup.add_argument('-s', '--scenario-setup', type=str, metavar='SCENARIO_IDENTIFIER', help='Set up the execution environment. This involves everything up to and including copying and installing the applications that are paret of the scenario')
argumentCommandGroup.add_argument('-x', '--scenario-execute', type=str, metavar='SCENARIO_IDENTIFIER', help='Start the run exection. If the scenario has not been set up, this could cause unexpected behavior!')
# parser.add_argument('-e', '--setup-environment', action='store_true', help='When provided with a scenario run, set up and clear the environment if necessary')
# parser.add_argument('-fe', '--force-setup-environment', action='store_true', help='When provided with a scenario run, set up and clear the environment, even if one is currently set up')
parser.add_argument('-g', '--gui', action='store_true', help='Display the UI for execution environments where one is available.')
parser.add_argument('-w', '--wipe-existing-environment', action='store_true', help='Will wipe any existing environment in its entirety and set everything up from scratch')
parser.add_argument('-k', '--keep-constructed-environment-running', action='store_true', help='Will keep the environment running after execution is finished (primarily used for debugging')


def _exit_handler(signal, frame):
    for file_object in _file_objects:
        if file_object is not None:
            file_object.flush()
            file_object.close()

signal.signal(signal.SIGINT, _exit_handler)
_file_objects = []


class ScenarioRunner:

    def __init__(self, scenario_identifier, do_setup_environments, do_setup_applications, do_execute_scenario, do_display_ui):
        self.scenario_identifier = scenario_identifier
        self.do_setup_environments = do_setup_environments
        self.do_setup_applications = do_setup_applications
        self.do_execute_scenario = do_execute_scenario
        self.config = config.scenarios[scenario_identifier]
        self.scenario_root = os.path.join(config.runtime_rootpath, 'scenarios', scenario_identifier)
        self.applications = []
        # self.behavior_validator = BehaviorValidator()
        config.display_ui = do_display_ui

    def execute_scenario(self):
        if ImmortalsGlobals.wipe_existing_environment:
            self.wipe_environment()

        if self.do_setup_environments:
            self.setup_environment()

        if self.do_setup_applications:
            self.setup_applications()

        if self.do_execute_scenario:

            self.start_applications()
            # self.behavior_validator.start_validation()

            if self.config.duration_seconds > 0:
                # Sleep a while for the test to run
                time.sleep(self.config.duration_seconds)

            # Stop all clients
            for application in self.applications:
                application.stop_application()

            # Stop collecting data and determine if the test passes
            # if self.config.duration_seconds > 0:
                # validation_result = self.behavior_validator.stop_and_validate()
            # else:
                # validation_result = True

            #Save the results to the results file and display the status on the screen
            # results = {}
            # results['testPass'] = validation_result

            # with open(os.path.join(self.scenario_root, 'results.json'), 'w') as resultFile:
                # _file_objects.append(resultFile)
                # commentjson.dump(results, resultFile)

            # print "Valiation Result: " + 'PASS' if validation_result else 'FAIL'

            if not ImmortalsGlobals.keep_constructed_environment_running:
                for application in self.applications:
                    application.stop_environment()


    def setup_environment(self):
        if os.path.exists(self.scenario_root):
            raise Exception('Attempted to run "setup_environment" over an existing environment!')

        # Make directories
        os.makedirs(self.scenario_root)

        for application in self.config.deployment_applications:
            os.mkdir(os.path.join(self.scenario_root, application.instance_identifier))


    """
    Remove the test environment folder if it exists
    """
    def wipe_environment(self):
        if os.path.exists(self.scenario_root):
            shutil.rmtree(self.scenario_root)


    def setup_applications(self):
        if len(self.applications) == 0:
            self._init_applications()

        for application in self.applications:
            if self.do_setup_environments:
                application.setup_start_environment()

            if self.do_setup_applications:
                application.setup_application()


    def start_applications(self):
        if len(self.applications) == 0:
            self._init_applications()

        for application in self.applications:
            # self.behavior_validator.add_client(application.instance_identifier, application.application_identifier)
            with open(os.path.join(application.root_path, 'log.txt'), 'w') as log:
                _file_objects.append(log)
                # application.start_application(self.behavior_validator.receive_event)
                application.start_application(None)


    def _init_applications(self):
        deployment_applications = self.config.deployment_applications

        for configuration_instance in deployment_applications:
            execution_path = os.path.join(self.scenario_root, configuration_instance.instance_identifier)
            application_instance = platformhelper.create_application_instance(execution_path, configuration_instance)
            self.applications.append(application_instance)


def main():
    args = parser.parse_args()

    if args.run_scenario:
        scenario_identifier = args.run_scenario
    elif args.scenario_setup:
        scenario_identifier = args.scenario_setup
    elif args.scenario_execute:
        scenario_identifier = args.scenario_execute

    if args.wipe_existing_environment:
        ImmortalsGlobals.wipe_existing_environment = args.wipe_existing_environment

    if args.keep_constructed_environment_running:
        ImmortalsGlobals.keep_constructed_environment_running = args.keep_constructed_environment_running



    # do_wipe_environments = args.force_setup_environment
    do_application_setup = args.run_scenario or args.scenario_setup
    do_environment_setup = args.run_scenario or args.scenario_setup
    do_execute_scenario = args.run_scenario or args.scenario_execute
    do_display_ui = args.gui

    # print 'do_wipe_environments=' + str(do_wipe_environments)
    print 'do_application_setup=' + str(do_application_setup)
    print 'do_environment_setup=' + str(do_environment_setup)
    print 'do_execute_scenario=' + str(do_execute_scenario)

    if args.list_scenarios:
        for test in config.scenarios.keys():
            print test

    elif args.run_scenario or args.scenario_setup or args.scenario_execute:
        if scenario_identifier not in config.scenarios:
            print 'The test identifier "' + scenario_identifier + '" is not a valid test. Valid tests:'
            for scenario in config.scenarios.keys():
                print scenario
        else:
            runner = ScenarioRunner(scenario_identifier, do_environment_setup, do_application_setup, do_execute_scenario, do_display_ui)
            runner.execute_scenario()

    else:
        parser.print_help()


if __name__ == '__main__':
    main()
