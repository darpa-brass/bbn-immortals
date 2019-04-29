import os
from enum import Enum

from pkg_resources import resource_string

SCRIPT_DIRECTORY = os.path.dirname(os.path.realpath(__file__))


class ScenarioType(Enum):
    Scenario5 = 5
    Scenario6 = 6


class Scenario(Enum):
    s5proto = (
        ScenarioType.Scenario5,
        'IMMORTALS_TEST-SCENARIO_5',
        'Scenario 5',
        os.path.join(SCRIPT_DIRECTORY, 'resources', 'dummy_data', 's5_dauInventory.xml'),
        os.path.join(SCRIPT_DIRECTORY, 'resources', 'dummy_data', 'scenario5_input_mdlRoot.xml'),
        None
    )

    s6a = (
        ScenarioType.Scenario6,
        'IMMORTALS_TEST-SCENARIO_6-UNKNOWN_SCHEMA',
        'Scenario 6 - Unknown Schema',
        None,
        None,
        resource_string('odbhelper.resources.dummy_data', 's6_advanced.json').decode()
    )
    s6b = (
        ScenarioType.Scenario6,
        'IMMORTALS_TEST-SCENARIO_6-KNOWN_SCHEMA',
        'Scenario 6 - Known Schema',
        None,
        None,
        resource_string('odbhelper.resources.dummy_data', 's6_basic.json').decode()
    )

    def __init__(self, scenario: ScenarioType, db_name: str, display_name: str, xml_inventory_path: str,
                 xml_input_path: str, json_input_string: str):
        self.scenario = scenario
        self.xml_inventory_path = xml_inventory_path
        self.xml_input_path = xml_input_path
        self.json_input_string = json_input_string

        if scenario == ScenarioType.Scenario5:
            assert xml_inventory_path is not None
            assert xml_input_path is not None
            assert json_input_string is None

        elif scenario == ScenarioType.Scenario6:
            assert xml_inventory_path is None
            assert xml_input_path is None
            assert json_input_string is not None
