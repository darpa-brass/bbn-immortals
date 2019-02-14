from threading import RLock
from typing import Set

import time

from pymmortals import immortalsglobals as ig
from pymmortals.datatypes.interfaces import AbstractMonitor, ValidatorInterface
from pymmortals.datatypes.routing import EventTag, EventType, EventTags
from pymmortals.datatypes.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.analyticsevent import AnalyticsEvent
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.status import Status
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testresult import TestResult
from pymmortals.generated.mil.darpa.immortals.core.api.validation.results.validationresults import ValidationResults
from pymmortals.routing.eventrouter import EventReceiverInterface


class AbstractLocalValidator(EventReceiverInterface, ValidatorInterface):
    def __init__(self, gif: GmeInterchangeFormat, runner_configuration: ScenarioRunnerConfiguration):
        self.gif: GmeInterchangeFormat = gif
        self.runner_configuration: ScenarioRunnerConfiguration = runner_configuration
        self._current_result: TestResult = TestResult(validatorIdentifier=self.identifier(),
                                                      currentState=Status.PENDING,
                                                      errorMessages=list(), detailMessages=list())
        self._lock: RLock = RLock()
        self._startTimeMS = time.time()*1000

    def __str__(self):
        return self.identifier()

    def receive_event(self, event_tag: EventTag, data: AnalyticsEvent):
        with self._lock:
            self._receive_event(event_tag=event_tag, data=data)

    def _receive_event(self, event_tag: EventTag, data: AnalyticsEvent):
        raise NotImplementedError

    def start(self):
        with self._lock:
            ig.get_event_router().subscribe_listener(EventType.ANALYSIS, self.receive_event)
            self._current_result: TestResult = TestResult(
                validatorIdentifier=self.identifier(), currentState=Status.RUNNING,
                errorMessages=list(), detailMessages=list())

    @classmethod
    def identifier(cls) -> str:
        raise NotImplementedError

    def attempt_validation(self, terminal_state: bool):
        with self._lock:
            result = self._attempt_validation(terminal_state=terminal_state)
            if terminal_state:
                ig.get_event_router().unsubscribe_listener(EventType.ANALYSIS, self.receive_event)

        results = ValidationResults(testDurationMS=(time.time()*1000)-self._startTimeMS,
                                    results=[result])
        ig.get_event_router().submit(EventTags.ValidationTestResultsProduced, results)

    def _attempt_validation(self, terminal_state: bool) -> TestResult:
        raise NotImplementedError

    def run_time_ms(self) -> int:
        raise NotImplementedError

    @classmethod
    def get_monitor_classes(cls) -> Set[AbstractMonitor]:
        raise NotImplementedError
