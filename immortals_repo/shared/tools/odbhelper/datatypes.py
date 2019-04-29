import json
import os
from enum import Enum
from typing import Dict, Optional

from pkg_resources import resource_string

SCRIPT_DIRECTORY = os.path.dirname(os.path.realpath(__file__))

STARTUP_TIMEOUT_S = 6.0
SHUTDOWN_TIMEOUT_S = 10.0

STDOUT_LOG_BASE_NAME = 'immortals-orientdb%s-out.log'
STDERR_LOG_BASE_NAME = 'immortals-orientdb%s-err.log'


class GraphDetails(Enum):
    s5 = ('IMMORTALS_TEST-SCENARIO_5', 'Scenario 5')
    s6a = ('IMMORTALS_TEST-SCENARIO_6-UNKNOWN_SCHEMA', 'Scenario 6 - Unknown Schema')
    s6b = ('IMMORTALS_TEST-SCENARIO_6-KNOWN_SCHEMA', 'Scenario 6 - Known Schema')

    def __init__(self, db_name: str, display_name: str):
        self.db_name = db_name
        self.display_name = display_name


class ScenarioType(Enum):
    Scenario5 = 5
    Scenario6 = 6


# noinspection PyPep8Naming
class Scenario:

    def __init__(self, name: str, scenarioType: str, dbName: str, xmlInventoryPath: Optional[str] = None,
                 xmlMdlrootInputPath: Optional[str] = None, jsonInputPath: Optional[str] = None):
        self.name = name
        self.scenarioType = ScenarioType[scenarioType]
        self.dbName = dbName
        self.xmlInventoryPath = xmlInventoryPath
        self.xmlMdlrootInputPath = xmlMdlrootInputPath
        self.jsonInputPath = jsonInputPath

        if scenarioType == ScenarioType.Scenario5:
            assert xmlInventoryPath is not None
            assert xmlMdlrootInputPath is not None
            assert jsonInputPath is None

        elif scenarioType == ScenarioType.Scenario6:
            assert xmlInventoryPath is None
            assert xmlMdlrootInputPath is None
            assert jsonInputPath is not None


def get_scenarios() -> Dict[str, Scenario]:
    scenarios = dict()
    scenario_json = json.loads(resource_string('odbhelper.resources', 'scenarios.json').decode())  # type: Dict
    for scenario_json in scenario_json['scenarios']:
        scenarios[scenario_json['name']] = Scenario(**scenario_json)
    return scenarios


if __name__ == '__main__':
    get_scenarios()
