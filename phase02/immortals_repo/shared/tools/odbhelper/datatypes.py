from enum import Enum

STARTUP_TIMEOUT_S = 6.0
SHUTDOWN_TIMEOUT_S = 10.0

STDOUT_LOG_BASE_NAME = 'immortals-orientdb%s-out.log'
STDERR_LOG_BASE_NAME = 'immortals-orientdb%s-err.log'


class TestScenario(Enum):
    s5 = ('IMMORTALS_TEST-SCENARIO_5', 'Scenario 5')
    s6a = ('IMMORTALS_TEST-SCENARIO_6-UNKNOWN_SCHEMA', 'Scenario 6 - Unknown Schema')
    s6b = ('IMMORTALS_TEST-SCENARIO_6-KNOWN_SCHEMA', 'Scenario 6 - Known Schema')

    def __init__(self, db_name: str, display_name: str):
        self.db_name = db_name
        self.display_name = display_name
