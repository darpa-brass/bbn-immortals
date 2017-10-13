#!/usr/bin/env python

import os
import shutil
from threading import Thread
from typing import List

from pymmortals import threadprocessrouter as tpr
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat
from pymmortals.interfaces import AbstractApplication
from pymmortals.utils import path_helper
from pymmortals.validators.validator_manager import ValidatorManager
from . import applicationhelper
from .deploymentplatform import LifecycleInterface
from .platforms.android.emuhelper import reset_identifier_counter as reset_android_identifier_counter


class ScenarioRunner:
    def __init__(self,
                 runner_configuration: ScenarioRunnerConfiguration,
                 deployment_model: GmeInterchangeFormat
                 ):
        self._runner_configuration: ScenarioRunnerConfiguration = runner_configuration

        self._scenario_root: str = path_helper(False, get_configuration().immortalsRoot,
                                               runner_configuration.deploymentDirectory)

        self._validator_manager: ValidatorManager = ValidatorManager(
            gif=deployment_model,
            runner_configuration=runner_configuration
        )

        self.application_instances: List[AbstractApplication] = []

        self.clean_deployment_directory()
        self.setup_deployment_directory()

    def execute_scenario(self):
        for configuration_instance in self._runner_configuration.scenario.deploymentApplications:
            application_instance = applicationhelper.create_application_instance(configuration_instance)
            self.application_instances.append(application_instance)

        platforms = []
        for app in self.application_instances:
            platforms.append(app.platform)

        if get_configuration().validationEnvironment.lifecycle.setupEnvironment:
            self.setup_platforms(platforms,
                                 get_configuration().validationEnvironment.startAndroidEmulatorsSimultaneously)

        if get_configuration().validationEnvironment.lifecycle.setupApplications:
            self.setup_applications(self.application_instances, False)
            pass

        # TODO Ideally validation and timing would not be handled by the start_applications call. But it works for now.
        if get_configuration().validationEnvironment.lifecycle.executeScenario:
            self.start_applications(self.application_instances, False, True)

        if get_configuration().validationEnvironment.lifecycle.haltEnvironment:
            # TODO: Halt here
            pass

    def setup_deployment_directory(self):
        if os.path.exists(self._scenario_root):
            raise Exception('Attempted to run "setup_environment" over an existing environment!')

        # Make directories
        os.makedirs(self._scenario_root)

        for application in self._runner_configuration.scenario.deploymentApplications:
            os.mkdir(os.path.join(self._scenario_root, application.instanceIdentifier))

    def clean_deployment_directory(self):
        if os.path.exists(self._scenario_root):
            shutil.rmtree(self._scenario_root)

    def halt_scenario(self):
        # if results is not None:
        #     self.results = results

        # Stop all clients
        for application in self.application_instances:
            application.stop()

        config = get_configuration()

        if config.validationEnvironment.lifecycle.haltEnvironment:
            for application in self.application_instances:
                application.platform.stop()

        tpr.cleanup_the_dead()
        reset_android_identifier_counter()

    def setup_platforms(self, objects: List[LifecycleInterface], threaded: bool = False):
        if threaded:
            thread_pool = []
            for obj in objects:
                t = tpr.start_thread(thread_method=ScenarioRunner.setup_platforms, thread_args=[self, [obj], False])
                thread_pool.append(t)

            for t in thread_pool:  # type: Thread
                # Using a timeout is the only way to causes a join call to finish if the daemon thread is killed.
                t.join(99999)

        else:
            selc = get_configuration().validationEnvironment.setupEnvironmentLifecycle

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

    def setup_applications(self, objects: List[AbstractApplication], threaded: bool = False):
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

    def start_applications(self, objects: List[AbstractApplication], threaded: bool = False, validate: bool = False):

        if validate:
            if self._runner_configuration.scenario.validatorIdentifiers is not None \
                    and self._runner_configuration.validate:
                self._validator_manager.start()

        # Put the server at the head of the startup list
        startup_list: List[AbstractApplication] = list()
        for a in objects:
            if a.config.applicationIdentifier.lower() == 'ataklite':
                startup_list.append(a)
            else:
                startup_list.insert(0, a)

        if threaded:
            thread_pool = []
            for obj in startup_list:
                t = tpr.start_thread(thread_method=ScenarioRunner.start_applications,
                                     thread_args=[self, [obj], False, False])
                thread_pool.append(t)

            for t in thread_pool:  # type: Thread
                # Using a timeout is the only way to causes a join call to finish if the daemon thread is killed.
                t.join(99999)

        else:
            for o in startup_list:
                o.start()

        self._validator_manager.wait()

        self.halt_scenario()
