from typing import Set

from pymmortals.datatypes.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat


class AbstractMonitor:
    def start(self, duration_ms: int):
        raise NotImplementedError

    def stop(self):
        raise NotImplementedError

    def is_running(self) -> bool:
        raise NotImplementedError

    def __init__(self, gif: GmeInterchangeFormat, runner_configuration: ScenarioRunnerConfiguration):
        self._gif: GmeInterchangeFormat = gif
        self._runner_configuration: ScenarioRunnerConfiguration = runner_configuration

    @classmethod
    def identifier(cls) -> str:
        raise NotImplementedError


class ValidatorInterface:
    @classmethod
    def identifier(cls) -> str:
        raise NotImplementedError

    @classmethod
    def get_monitor_classes(cls) -> Set[AbstractMonitor]:
        raise NotImplementedError

    def start(self):
        raise NotImplementedError

    def run_time_ms(self) -> int:
        raise NotImplementedError

    def attempt_validation(self, terminal_state: bool):
        raise NotImplementedError
