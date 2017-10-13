import json
from typing import Dict, List

from pkg_resources import resource_string, resource_listdir

from pymmortals.utils import clean_json_str

def get_phase1_input_string(filename: str) -> str:
    return clean_json_str(resource_string('pymmortals.resources.deployment_models', filename + '.json'))


def get_phase1_input_dict(filename: str) -> Dict:
    return json.loads(clean_json_str(resource_string('pymmortals.resources.deployment_models', filename + '.json')))


def get_test_list() -> List[str]:
    test_list = list()
    test_files = resource_listdir('pymmortals.resources', "test_scenarios")
    for f in test_files:
        if not f.startswith('__'):
            test_list.append(f.replace('.json', ''))

    return test_list


def get_test_suite(identifier: str) -> Dict:
    return json.loads(
        clean_json_str(resource_string('pymmortals.resources.test_scenarios', identifier + '.json')))


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
