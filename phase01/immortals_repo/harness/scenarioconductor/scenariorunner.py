#!/usr/bin/env python

import argparse
import logging
import os
import shutil
import signal

from .data.base.tools import path_helper
from .data.base.validationresults import ValidationResults
from . import applicationhelper
from . import immortalsglobals as ig
from . import threadprocessrouter as tpr
from .android.androidplatform_emulator import reset_identifier_counter as ae_ric
from .behaviorvalidator import BehaviorValidator
from .data.applicationconfig import ApplicationConfig
from .data.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from .deploymentplatform import LifecycleInterface
from .packages import commentjson as json

logging.basicConfig(level=logging.DEBUG)

parser = argparse.ArgumentParser(description='IMMORTALS Scenario Runner')


def add_parser_arguments(psr):
    psr.add_argument('-f', '--configuration-file', type=str,
                     help='Loads all the configuration data from the specified file, ignoring any other parameters')
    psr.add_argument('-s', '--configuration-string', type=str,
                     help='Loads all the configuration data from the specified json string, ignoring any other parameters')


add_parser_arguments(parser)


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
    :type scenario_root: str
    :type behaviorvalidator: BehaviorValidator
    :type is_running: bool
    :type results: ValidationResults
    :type application_instances: list[LifecycleInterface]
    """

    def __init__(self,
                 runner_configuration,  # type: ScenarioRunnerConfiguration
                 ):
        ig.config = runner_configuration
        self.runner_configuration = runner_configuration

        self.scenario_root = path_helper(False, ig.IMMORTALS_ROOT, runner_configuration.deploymentDirectory)

        self.behaviorvalidator = None
        self.is_running = False
        self.results = None

        self.application_instances = []

    def execute_scenario(self):
        """
        :rtype: 
        """

        self.is_running = True

        self.clean_deployment_directory()
        self.setup_deployment_directory()

        for configuration_instance in self.runner_configuration.scenario.deploymentApplications:
            application_instance = applicationhelper.create_application_instance(configuration_instance)
            self.application_instances.append(application_instance)

        platforms = []
        for app in self.application_instances:
            platforms.append(app.platform)

        if self.runner_configuration.lifecycle.setupEnvironment:
            self.setup_platforms(platforms, self.runner_configuration.startEmulatorsSimultaneously)

        if self.runner_configuration.lifecycle.setupApplications:
            self.setup_applications(self.application_instances, False)
            pass

        # TODO Ideally validation and timing would not be handled by the start_applications call. But it works for now.
        if self.runner_configuration.lifecycle.executeScenario:
            self.start_applications(self.application_instances, False, True)

        if self.runner_configuration.lifecycle.haltEnvironment:
            # TODO: Halt here
            pass

        return self.results

    def setup_deployment_directory(self):
        if os.path.exists(self.scenario_root):
            raise Exception('Attempted to run "setup_environment" over an existing environment!')

        # Make directories
        os.makedirs(self.scenario_root)

        for application in self.runner_configuration.scenario.deploymentApplications:
            os.mkdir(os.path.join(self.scenario_root, application.instanceIdentifier))

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

        if self.runner_configuration.lifecycle.haltEnvironment:
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
            selc = self.runner_configuration.setupEnvironmentLifecycle

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
            if self.runner_configuration.scenario.validatorIdentifiers is not None and self.runner_configuration.validate:
                client_identifiers = []

                for client in self.runner_configuration.scenario.deploymentApplications:  # type: ApplicationConfig
                    if client.applicationIdentifier == 'ATAKLite' or client.applicationIdentifier == 'ataklite':
                        client_identifiers.append(client.instanceIdentifier)

                self.behaviorvalidator = BehaviorValidator(self.runner_configuration)
                self.behaviorvalidator.start_server()
                # self.behaviorvalidator.start(self.halt_scenario)

        # if threaded:
        #     thread_pool = []
        #     for obj in objects:
        #         t = tpr.start_thread(thread_method=ScenarioRunner.start_applications,
        #                              thread_args=[self, [obj], False, False])
        #         thread_pool.append(t)
        #
        #     for t in thread_pool:  # type: Thread
        #         # Using a timeout is the only way to causes a join call to finish if the daemon thread is killed.
        #         t.join(99999)
        #
        # else:
        #     for o in objects:
        #         o.start()

        for o in objects:
            o.start()

        result = self.behaviorvalidator.wait_for_validation_result()
        self.halt_scenario(result)


def main(args=None):
    if args is None:
        args = parser.parse_args()

    if args.configuration_file is not None:
        with open(args.configuration_file, 'r') as f:
            src_j = json.load(f)
            src = ScenarioRunnerConfiguration.from_dict(src_j, None)

        sr = ScenarioRunner(src)
        sr.execute_scenario()

    elif args.configuration_string is not None:
        src_j = json.load(args.configuration_string)
        src = ScenarioRunnerConfiguration.from_dict(src_j)
        sr = ScenarioRunner(src)
        sr.execute_scenario()


if __name__ == '__main__':
    main()
