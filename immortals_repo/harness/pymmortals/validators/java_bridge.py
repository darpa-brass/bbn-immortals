from threading import RLock
from typing import FrozenSet

import pymmortals.immortalsglobals as ig
from pymmortals import websocket
from pymmortals.datatypes.routing import EventTags
from pymmortals.datatypes.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat
from pymmortals.generated.mil.darpa.immortals.core.api.validation.results.validationresults import ValidationResults
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validationstartdata import ValidationStartData
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validators import Validators
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validatortype import ValidatorType
from pymmortals.websocket import DasBridge

_validation_running = False


class ValidatorManagerJavaBridge:
    def __init__(self, gif: GmeInterchangeFormat, runner_configuration: ScenarioRunnerConfiguration):
        self._gif: GmeInterchangeFormat = gif
        self._runner_configuration: ScenarioRunnerConfiguration = runner_configuration

        self._das_bridge: DasBridge = websocket.get_das_bridge()

        self._das_bridge.set_validation_results_listener(self._result_listener)

        # Collect the validators
        self._validators = [
            k for k in Validators if
            k.identifier in runner_configuration.scenario.validatorIdentifiers and
            k.validatorType == ValidatorType.JAVA
        ]

        self._client_identifiers: FrozenSet(str) = frozenset(
            [a.instanceIdentifier
             for a in runner_configuration.scenario.deploymentApplications
             if a.applicationIdentifier.lower() == 'ataklite'])

        self._network_thread = None
        self._lock: RLock = RLock()
        self._is_running = False

    def _result_listener(self, results: ValidationResults):
        with self._lock:
            ig.get_event_router().submit(EventTags.ValidationTestResultsProduced, results)
            self._is_running = False

    def start(self):
        with self._lock:
            # TODO: Get duration from tests
            vsd = ValidationStartData(
                minRuntimeMS=80000,
                maxRuntimeMS=80000,
                clientIdentifiers=list(self._client_identifiers),
                validatorIdentifiers=list([v.identifier for v in self._validators])
            )
            self._das_bridge.validationStart(vsd)
            self._is_running = True

    def stop(self):
        self._das_bridge.validationStop()

    def is_running(self) -> bool:
        return self._is_running
