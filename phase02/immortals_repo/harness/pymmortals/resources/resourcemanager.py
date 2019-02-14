import json
from typing import Dict, List

from pkg_resources import resource_string, resource_listdir

from pymmortals.utils import clean_json_str


def get_phase1_input_string(filename: str) -> str:
    return clean_json_str(resource_string('pymmortals.resources.deployment_models', filename + '.json'))


def get_phase1_input_dict(filename: str) -> Dict:
    return json.loads(clean_json_str(resource_string('pymmortals.resources.deployment_models', filename + '.json')))


def get_p1_test_list() -> List[str]:
    test_list = list()
    test_files = resource_listdir('pymmortals.resources', "p1_test_scenarios")
    for f in test_files:
        if not f.startswith('__'):
            test_list.append(f.replace('.json', ''))

    return test_list


def get_p2_test_suite_list() -> List[str]:
    test_suite_list = list()
    test_files = resource_listdir('pymmortals.resources', "p2_test_scenarios")
    for f in test_files:
        if not f.startswith('__'):
            test_suite_list.append(f.replace('.json', ''))

    return test_suite_list


def get_p2_test_suite(suite_identifier: str) -> Dict:
    return json.loads(clean_json_str(resource_string('pymmortals.resources.p2_test_scenarios',
                                                     suite_identifier + '.json')))


def get_p2_unified_test_suite_and_test_list() -> List[str]:
    test_list = list()
    test_files = resource_listdir('pymmortals.resources', "p2_test_scenarios")
    for f in test_files:
        if not f.startswith('__'):
            test_suite_name = f.replace('.json', '')
            test_list.append(test_suite_name)
            test_names = get_p2_test_suite_test_list(test_suite_name)
            for i in range(len(test_names)):
                test_list.append(test_suite_name + '.' + test_names[i])

    return test_list


def get_p2_test_suite_test_list(suite_identifier: str):
    test_list = json.loads(clean_json_str(resource_string('pymmortals.resources.p2_test_scenarios',
                                                          suite_identifier + '.json')))
    test_identifier_list = [k['scenarioIdentifier'] for k in test_list]
    return test_identifier_list


def get_p2_test(suite_identifier: str, test_identifier: str) -> Dict:
    test_suite = get_p2_test_suite(suite_identifier=suite_identifier)
    return [k for k in test_suite if k['scenarioIdentifier'] == test_identifier][0]


def get_p1_test_suite(identifier: str) -> Dict:
    return json.loads(
        clean_json_str(resource_string('pymmortals.resources.p1_test_scenarios', identifier + '.json')))


def load_bandwidth_analysis_configuration_dict() -> Dict:
    return json.loads(
        clean_json_str(
            resource_string('pymmortals.resources.platform_analysis', 'calculated_bandwidth_visualizations.json')))


def load_canned_emulator_analysis_data_dict_list() -> List[Dict]:
    return json.loads(clean_json_str(
        resource_string('pymmortals.resources.platform_analysis', 'emulator_analysis_data.json')))


def load_configuration_dict(filename: str) -> Dict[str, object]:
    return json.loads(clean_json_str(resource_string('pymmortals.resources.configuration',
                                                     filename + ('' if filename.endswith('.json') else '.json'))))


def load_installation_configuration() -> Dict:
    return json.loads(clean_json_str(resource_string('pymmortals.resources', 'installation_configuration.json')))


def load_immortals_configuration() -> Dict:
    return json.loads(clean_json_str(resource_string('pymmortals.resources', 'immortals_config.json')))
