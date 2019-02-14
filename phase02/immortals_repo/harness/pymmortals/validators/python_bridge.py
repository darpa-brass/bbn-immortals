import time
from threading import Event
from typing import Dict, List, Set

from pymmortals import immortalsglobals as ig
from pymmortals import threadprocessrouter as tpr
from pymmortals.datatypes.interfaces import AbstractMonitor, ValidatorInterface
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.routing import EventTags
from pymmortals.datatypes.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from pymmortals.datatypes.validation import load_python_validator_class
from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testresult import TestResult
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validationstartreturndata import \
    ValidationStartReturnData
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validators import Validators
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validatortype import ValidatorType


class PythonValidatorManager:
    def __init__(self, gif: GmeInterchangeFormat, runner_configuration: ScenarioRunnerConfiguration):
        self._gif: GmeInterchangeFormat = gif
        self._runner_configuration: ScenarioRunnerConfiguration = runner_configuration

        # Determine the validators
        self._validators = [k for k in Validators if k.identifier in runner_configuration.scenario.validatorIdentifiers
                            and k.validatorType == ValidatorType.PYTHON]

        self._monitors: Dict[str, AbstractMonitor] = dict()
        self._running_pending_validators: Dict[str, ValidatorInterface] = dict()
        self._validator_results: List[TestResult] = []
        self._done_event: Event = Event()

        monitor_classes: Set[AbstractMonitor] = set()

        for i in self._validators:
            v: ValidatorInterface = load_python_validator_class(i)(gif, runner_configuration)
            monitor_classes = monitor_classes.union(v.get_monitor_classes())
            self._running_pending_validators[i] = v

        for i in monitor_classes:
            self._monitors[i] = i(gif, runner_configuration)

        self._start_time_ms: int = None

    def start(self):
        ig.get_event_router().submit(EventTags.ValidationStarted,
                                     ValidationStartReturnData(
                                         expectedDurationSeconds=self.get_duration(),
                                         validatorIdentifiers=[k.identifier for k in self._validators]))
        self._start_time_ms = time.time() * 1000

        for v in list(self._running_pending_validators.values()):
            v.start()

        for m in list(self._monitors.values()):
            m.start(duration_ms=self.get_duration())

        tpr.start_timer(duration_seconds=self.get_duration() / 1000, shutdown_method=self._shutdown)

    def get_duration(self):
        duration = get_configuration().validation.minimumTestDurationMS
        for v in list(self._running_pending_validators.values()):
            duration = max(duration, v.run_time_ms())

        return duration

    def stop(self):
        for v in self._running_pending_validators.values():  # type: ValidatorInterface
            v.attempt_validation(terminal_state=True)

    def _shutdown(self):
        for v in self._running_pending_validators.values():
            v.attempt_validation(terminal_state=True)
