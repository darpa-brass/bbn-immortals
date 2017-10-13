import json
import os
import time
from threading import Event
from typing import List

import pymmortals.auxillary_functions
import pymmortals.auxillary_functions
from pymmortals import immortalsglobals as ig
from pymmortals import threadprocessrouter as tpr
from pymmortals.datatypes.routing import EventTags, EventTag
from pymmortals.datatypes.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.status import Status
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testdetails import TestDetails
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testresult import TestResult
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.validationstate import ValidationState
from pymmortals.generated.mil.darpa.immortals.core.api.validation.results.validationresults import ValidationResults
from pymmortals.validators.java_bridge import ValidatorManagerJavaBridge
from pymmortals.validators.python_bridge import PythonValidatorManager


def _process_results(test_results_list: List[TestResult], gif: GmeInterchangeFormat) -> ValidationState:
    # Calculate the validators
    validators = pymmortals.auxillary_functions.calculate_validator_identifiers(gif)

    # Transform the details returned from the validation server to a mapping of test identifiers to the details
    # formatted for LL consumption
    test_result_map = {
        k.validatorIdentifier:
            TestDetails(
                testIdentifier=k.validatorIdentifier,
                expectedStatus=(
                    Status.NOT_APPLICABLE if k.validatorIdentifier not in validators
                    else Status.SUCCESS if validators[k.validatorIdentifier] else Status.FAILURE
                ),
                actualStatus=k.currentState,
                details=k
            )
        for k in test_results_list}

    overall_pass = True

    for validator in list(test_result_map.keys()):
        td = test_result_map[validator]  # type: TestDetails

        if td.expectedStatus != td.actualStatus:
            if td.expectedStatus is Status.SUCCESS or td.expectedStatus is Status.FAILURE:
                overall_pass = False
                break

    return ValidationState(
        executedTests=list(test_result_map.values()),
        overallIntentStatus=Status.SUCCESS if overall_pass else Status.FAILURE
    )


class ValidatorManager:
    def __init__(self, gif: GmeInterchangeFormat, runner_configuration: ScenarioRunnerConfiguration):
        self._gif: GmeInterchangeFormat = gif
        self._runner_configuration: ScenarioRunnerConfiguration = runner_configuration

        self._java_validator: ValidatorManagerJavaBridge = \
            ValidatorManagerJavaBridge(gif=gif, runner_configuration=runner_configuration)

        self._python_validator: PythonValidatorManager = \
            PythonValidatorManager(gif=gif, runner_configuration=runner_configuration)

        self._start_time_ms: int = None

        self._validator_results: List[TestResult] = list()

        self._done_event: Event = Event()

    def start(self):
        ig.get_event_router().subscribe_listener(EventTags.ValidationTestResultsProduced, self.receive_results)

        if not os.path.exists(self._runner_configuration.deploymentDirectory + 'results/'):
            os.mkdir(self._runner_configuration.deploymentDirectory + 'results/')

        self._java_validator.start()
        self._python_validator.start()

        self._start_time_ms = time.time() * 1000

    def wait(self):
        # TODO: Get this the right way...
        tpr.sleep(60)
        self._java_validator.stop()
        self._python_validator.stop()

        self._done_event.wait()

    # noinspection PyUnusedLocal
    def receive_results(self, event_tag: EventTag, results: ValidationResults):
        for result in results.results:
            self._validator_results.append(result)

        if len(self._validator_results) == len(self._runner_configuration.scenario.validatorIdentifiers):
            ig.get_event_router().unsubscribe_listener(EventTags.ValidationTestResultsProduced, self.receive_results)

            v = ValidationResults((time.time() * 1000) - self._start_time_ms, self._validator_results)

            with open(self._runner_configuration.deploymentDirectory + 'results/evaluation_result.json', 'w') as f:
                json.dump(v.to_dict(include_metadata=False), f)

            validation_state = _process_results(test_results_list=self._validator_results,
                                                gif=self._gif)

            ig.get_event_router().submit(EventTags.ValidationTestsFinished, validation_state)

            self._done_event.set()
