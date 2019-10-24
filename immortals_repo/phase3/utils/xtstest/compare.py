#!/usr/bin/env python

import argparse
import json
from typing import Dict

parser = argparse.ArgumentParser()
parser.add_argument('--initial-file', '-i', type=str, required=True, help='The initial test results')
parser.add_argument('--updated-file', '-u', type=str, required=True, help='The updated test results')


def delta_err_dicts(initial_error_dict: Dict, updated_error_dict: Dict) -> Dict:
    err_type_change = dict()

    initial_error_types = set(initial_error_dict.keys())
    updated_error_types = set(updated_error_dict.keys())

    shared_errors = initial_error_types.intersection(updated_error_types)
    initial_error_types.difference_update(shared_errors)
    updated_error_types.difference_update(shared_errors)

    for err_key in shared_errors:
        initial_value = initial_error_dict[err_key]
        updated_value = updated_error_dict[err_key]
        delta = updated_value - initial_value
        err_type_change[err_key] = delta

    for err_key in initial_error_types:
        err_type_change[err_key] = -initial_error_dict[err_key]

    for err_key in updated_error_types:
        err_type_change[err_key] = updated_error_dict[err_key]

    return err_type_change


def parse_scenario_set(initial_json_data: Dict, updated_json_data: Dict) -> Dict:
    rval = dict()

    initial_errors = initial_json_data['totalErrors']
    updated_errors = updated_json_data['totalErrors']

    rval['errorChange'] = updated_errors - initial_errors
    rval['errorTypeChange'] = delta_err_dicts(initial_json_data['errorTypeCounts'], updated_json_data['errorTypeCounts'])
    rval['errorSubtypeChange'] = delta_err_dicts(initial_json_data['errorSubtypeCounts'],
                                                 updated_json_data['errorSubtypeCounts'])

    return rval


def parse_error_delta(initial_json_data: Dict, updated_json_data: Dict) -> Dict:
    rval = dict()

    rval['allScenarios'] = parse_scenario_set(initial_json_data['allScenarios'], updated_json_data['allScenarios'])
    rval['customScenarios'] = parse_scenario_set(initial_json_data['customScenarios'],
                                                 updated_json_data['customScenarios'])
    rval['predefinedScenarios'] = parse_scenario_set(initial_json_data['predefinedScenarios'],
                                                     updated_json_data['predefinedScenarios'])

    print(json.dumps(rval, indent=4))
    return rval


if __name__ == '__main__':
    args = parser.parse_args()
    initial = json.load(open(args.initial_file))
    updated = json.load(open(args.updated_file))
    parse_error_delta(initial, updated)
