import json
import os
from enum import Enum
from typing import Optional, List, Dict

STARTUP_TIMEOUT_S = 6.0
SHUTDOWN_TIMEOUT_S = 10.0

STDOUT_LOG_BASE_NAME = 'immortals-orientdb-out.log'
STDERR_LOG_BASE_NAME = 'immortals-orientdb-err.log'

SCRIPT_DIRECTORY = os.path.dirname(os.path.realpath(__file__))


def clean_json_lines(lines):
    """
    :type lines: list[str] or list[bytes]
    :rtype: list[str]
    """
    return_lines = list()
    for line in lines:
        if isinstance(line, bytes):
            line = line.decode()

        s_l = line.strip()
        if not s_l.startswith('//') and not s_l.startswith('#'):
            return_lines.append(s_l)

    return return_lines


def clean_json_str(s):
    """
    :type s: str or bytes
    :rtype: str
    """
    if isinstance(s, bytes):
        s = s.decode()
    stripped = s.strip()

    if '\n' in stripped:
        lines = stripped.split('\n')
        return ''.join(clean_json_lines(lines))


class ScenarioType(Enum):
    Scenario5 = 5
    Scenario6 = 6


# noinspection PyPep8Naming
class Scenario:

    def __init__(self, shortName: str, prettyName: str, scenarioType: str, timeoutMS: int,
                 expectedStatusSequence: List[str], xmlInventoryPath: Optional[str] = None,
                 xmlMdlrootInputPath: Optional[str] = None, jsonInputPath: Optional[str] = None,
                 expectedJsonOutputStructure: Optional[Dict] = None,
                 ingestedXmlInventoryHash: Optional[str] = None,
                 ingestedXmlMdlrootInputHash: Optional[str] = None
                 ):
        self.shortName = shortName
        self.prettyName = prettyName
        self.scenarioType = ScenarioType[scenarioType]
        self.timeoutMS = timeoutMS
        self.dbName = 'IMMORTALS_' + shortName
        self.expectedStatusSequence = expectedStatusSequence
        self.xmlInventoryPath = xmlInventoryPath
        self.xmlMdlrootInputPath = xmlMdlrootInputPath
        self.jsonInputPath = jsonInputPath
        self.expectedJsonOutputStructure = expectedJsonOutputStructure
        self.ingestedXmlInventoryHash = ingestedXmlInventoryHash
        self.ingestedXmlMdlrootInputHash = ingestedXmlMdlrootInputHash

        if scenarioType == ScenarioType.Scenario5:
            assert xmlInventoryPath is not None
            assert xmlMdlrootInputPath is not None
            assert jsonInputPath is None

        elif scenarioType == ScenarioType.Scenario6:
            assert xmlInventoryPath is None
            assert xmlMdlrootInputPath is None
            assert jsonInputPath is not None


def get_scenarios(scenario_definitions_file: str = None) -> Dict[str, Scenario]:
    scenarios = dict()

    if scenario_definitions_file is None:
        base_scenarios_path = os.path.abspath(
            os.path.join(SCRIPT_DIRECTORY, '../../../', 'phase3/immortals-orientdb-server/src/main/resources/'))
        s5_json = json.loads(clean_json_str(open(os.path.join(base_scenarios_path, 's5_scenarios.json')).read()))
        s6_json = json.loads(clean_json_str(open(os.path.join(base_scenarios_path, 's6_scenarios.json')).read()))
        json_data = s5_json['scenarios'] + s6_json['scenarios']

    else:
        json_data = json.loads(open(scenario_definitions_file))['scenarios']

    for scenario_json in json_data:
        if 'scenarioType' not in scenario_json:
            if 'xmlInventoryPath' in scenario_json and 'xmlMdlrootInputPath' in scenario_json:
                # if scenario_json['xmlInventoryPath'] is not None and scenario_json['xmlMdlrootInputPath'] is not None:
                scenario_json['scenarioType'] = 'Scenario5'

            elif 'jsonInputPath' in scenario_json:
                # elif scenario_json['jsonInputPath'] is not None:
                scenario_json['scenarioType'] = 'Scenario6'

            else:
                raise Exception("Could not determine scenario type!")

        scenarios[scenario_json['shortName']] = Scenario(**scenario_json)

    return scenarios
