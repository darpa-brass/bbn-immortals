from enum import Enum
from typing import Dict, FrozenSet

from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.dasoutcome import DasOutcome
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.verdictoutcome import VerdictOutcome
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.submissionmodel import SubmissionModel
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.testadapterendpoint import TestAdapterEndpoint
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.testharnessendpoint import TestHarnessEndpoint
from .serializable import Serializable, ValuedEnum


class Phase2SubmissionFlow(Enum):
    BaselineA = 'BaselineA'
    BaselineB = 'BaselineB'
    Challenge = 'Challenge'


class PerturbationScenario(ValuedEnum):
    P2CP1DatabaseSchema = TestAdapterEndpoint.CP1
    P2CP2DataInTransit = TestAdapterEndpoint.CP2
    P2CP3LibraryOrPlatformUpgrade = TestAdapterEndpoint.CP3

    P2CP3AndroidPlatform = TestAdapterEndpoint.CP3
    P2CP3AndroidPartialLibraryUpgrade = TestAdapterEndpoint.CP3
    # P2CP3AndroidLibraryMutation = TestAdapterEndpoint.CP3
    # P2CP3JavaLibraryUpgrade = TestAdapterEndpoint.CP3
    P2CP3JavaLibraryMutation = TestAdapterEndpoint.CP3

    def __init__(self, endpoint: TestAdapterEndpoint):
        self.endpoint = endpoint

    @property
    def identifier(self) -> str:
        return self._value_

    @classmethod
    def all_identifiers(cls) -> FrozenSet[str]:
        return cls._values()


class Phase2TestHarnessListenerInterface:
    def receiving_post_listener(self, endpoint: TestHarnessEndpoint, body_str: str):
        raise NotImplementedError

    def sending_post_listener(self, endpoint: TestHarnessEndpoint, body_dict: Dict):
        raise NotImplementedError

    def received_post_ack_listener(self, endpoint: TestHarnessEndpoint, response_code: int, body_str: str):
        raise NotImplementedError

    def sent_post_ack_listener(self, endpoint: TestHarnessEndpoint, response_code: int, body_str: str):
        raise NotImplementedError


# noinspection PyPep8Naming
class Phase2TestScenario(Serializable):
    _validator_values = {}

    def __init__(self,
                 submissionFlow: Phase2SubmissionFlow,
                 perturbationScenario: PerturbationScenario,
                 expectedAdaptationResult: DasOutcome,
                 expectedVerdictOutcome: VerdictOutcome,
                 scenarioIdentifier: str,
                 submissionModel: SubmissionModel):
        super().__init__()
        self.submissionFlow = submissionFlow
        self.perturbationScenario = perturbationScenario
        self.expectedAdaptationResult = expectedAdaptationResult
        self.expectedVerdictOutcome = expectedVerdictOutcome
        self.scenarioIdentifier = scenarioIdentifier
        self.submissionModel = submissionModel
