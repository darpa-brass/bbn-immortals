#!/usr/bin/env python

import argparse
import logging
import os
import shutil
import signal
import time

import applicationhelper
import immortalsglobals
import threadprocessrouter as tpr
from android.androidplatform_emulator import reset_identifier_counter as ae_ric
from behaviorvalidator import BehaviorValidator
from configurationmanager import Configuration, path_helper
from data.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from deploymentplatform import LifecycleInterface

logging.basicConfig(level=logging.DEBUG)

parser = argparse.ArgumentParser(description='IMMORTALS Scenario Runner')
agc = parser.add_mutually_exclusive_group()
agc.add_argument('-l', '--list-scenarios', action='store_true', help='List available scenarios')
agc.add_argument('-r', '--run-scenario', type=str, metavar='SCENARIO_IDENTIFIER',
                 help='Restart the adb server and run the default test scenario')
agc.add_argument('-s', '--scenario-setup', type=str, metavar='SCENARIO_IDENTIFIER',
                 help='Set up the execution environment. This involves everything up to and including copying and ' +
                      'installing the applications that are parent of the scenario')
agc.add_argument('-x', '--scenario-execute', type=str, metavar='SCENARIO_IDENTIFIER',
                 help='Start the run execution. If the scenario has not been set up, this could cause unexpected ' +
                      'behavior!')
parser.add_argument('-g', '--gui', action='store_true',
                    help='Display the UI for execution environments where one is available.')
parser.add_argument('-w', '--wipe-existing-environment', action='store_true',
                    help='Will wipe any existing environment in its entirety and set everything up from scratch')
parser.add_argument('-k', '--keep-constructed-environment-running', action='store_true',
                    help='Will keep the environment running after execution is finished (primarily used for debugging')
parser.add_argument('-v', '--perform-validation', action='store_true',
                    help='Will execute validation of the scenario(s) specified to be run')
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
    """

    def __init__(self,
                 runner_configuration,  # type: ScenarioRunnerConfiguration
                 ):
        immortalsglobals.config = runner_configuration
        self.runner_config = runner_configuration

        self.scenario_root = path_helper(False, Configuration.immortals_root, runner_configuration.deployment_directory)

        self.behaviorvalidator = None
        self.is_running = False
        self.results = None

        self.application_instances = []

    def execute_scenario(self):
        self.is_running = True

        self.clean_deployment_directory()
        self.setup_deployment_directory()

        for configuration_instance in self.runner_config.scenario.deployment_applications:
            application_instance = applicationhelper.create_application_instance(configuration_instance)
            self.application_instances.append(application_instance)

        platforms = []
        for app in self.application_instances:
            platforms.append(app.platform)

        if self.runner_config.lifecycle.setupEnvironment:
            self.setup_platforms(platforms, self.runner_config.start_emulators_simultaneously)

        if self.runner_config.lifecycle.setupApplications:
            self.setup_applications(self.application_instances, False)
            pass

        if self.runner_config.lifecycle.executeScenario:
            self.start_applications(self.application_instances, False, True)

        # Just in case there is an issue and the halt does not get called...
        duration = self.runner_config.minDurationMS if self.runner_config.minDurationMS > self.runner_config.scenario.durationMS else self.runner_config.scenario.durationMS
        counter = 0
        while self.is_running and counter < duration:
            time.sleep(1)
            # counter += 1

        if self.runner_config.lifecycle.haltEnvironment:
            # TODO: Halt here
            pass

        return self.results

    def setup_deployment_directory(self):
        if os.path.exists(self.scenario_root):
            raise Exception('Attempted to run "setup_environment" over an existing environment!')

        # Make directories
        os.makedirs(self.scenario_root)

        for application in self.runner_config.scenario.deployment_applications:
            os.mkdir(os.path.join(self.scenario_root, application.instance_identifier))

    """
    Remove the test environment folder if it exists
    """

    def clean_deployment_directory(self):
        if os.path.exists(self.scenario_root):
            shutil.rmtree(self.scenario_root)

    def halt_scenario(self, results=None):
        if results is not None:
            self.results = results

        # Stop all clients
        for application in self.application_instances:
            application.stop()

        if self.runner_config.lifecycle.haltEnvironment:
            for application in self.application_instances:
                application.platform.stop()

        if self.behaviorvalidator is not None:
            self.behaviorvalidator.stop()

        tpr.cleanup_the_dead()
        ae_ric()

        self.is_running = False

    def setup_platforms(self, objects, threaded=False):
        """
        :type objects: list[LifecycleInterface]
        :type threaded: bool
        """

        if threaded:
            thread_pool = []
            for obj in objects:
                t = tpr.start_thread(thread_method=ScenarioRunner.setup_platforms, thread_args=[self, [obj], False])
                thread_pool.append(t)

            for t in thread_pool:  # type: Thread
                # Using a timeout is the only way to causes a join call to finish if the daemon thread is killed.
                t.join(99999)

        else:
            selc = self.runner_config.setupEnvironmentLifecycle

            if selc.destroyExisting:
                for o in objects:
                    o.destroy()

                o.setup()

            elif selc.cleanExisting:
                for o in objects:
                    if not o.is_setup():
                        o.setup()

                    if not o.is_running():
                        o.start()

            else:
                for o in objects:
                    if not o.is_setup():
                        o.setup()

            for o in objects:
                o.start()

    def setup_applications(self, objects, threaded=False):
        """
        :type objects: list[LifecycleInterface]
        :type threaded: bool
        """

        if threaded:
            thread_pool = []
            for obj in objects:
                t = tpr.start_thread(thread_method=ScenarioRunner.setup_applications, thread_args=[self, [obj], False])
                thread_pool.append(t)

            for t in thread_pool:  # type: Thread
                # Using a timeout is the only way to causes a join call to finish if the daemon thread is killed.
                t.join(99999)

        else:
            for o in objects:
                o.setup()

    def start_applications(self, objects, threaded=False, validate=False):

        if validate:
            if self.runner_config.scenario.validator_identifiers is not None and self.runner_config.validate:
                client_identifiers = []

                for client in self.runner_config.scenario.deployment_applications:  # type: ApplicationConfig
                    if client.application_identifier == 'ATAKLite':
                        client_identifiers.append(client.instance_identifier)

                self.behaviorvalidator = BehaviorValidator(self.runner_config)
                self.behaviorvalidator.start(self.halt_scenario)

        if threaded:
            thread_pool = []
            for obj in objects:
                t = tpr.start_thread(thread_method=ScenarioRunner.setup_applications,
                                     thread_args=[self, [obj], False, False])
                thread_pool.append(t)

            for t in thread_pool:  # type: Thread
                # Using a timeout is the only way to causes a join call to finish if the daemon thread is killed.
                t.join(99999)

        else:
            for o in objects:
                o.start()
