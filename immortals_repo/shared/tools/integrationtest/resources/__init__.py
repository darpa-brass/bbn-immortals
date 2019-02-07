import json
from threading import RLock
from typing import Dict, List, Union

from pkg_resources import resource_string, resource_listdir

_test_identifier_suite_identifiers = dict()

# _test_scenario_identifier_file_dict = dict()

_lock = RLock()


def _clean_json_lines(lines: Union[List[str], List[bytes]]) -> List[str]:
    return_lines = list()
    for line in lines:
        if isinstance(line, bytes):
            line = line.decode()

        s_l = line.strip()
        if not s_l.startswith('//') and not s_l.startswith('#'):
            return_lines.append(s_l)

    return return_lines


def _clean_json_str(s: Union[str, bytes]) -> str:
    if isinstance(s, bytes):
        s = s.decode()
    stripped = s.strip()

    if '\n' in stripped:
        lines = stripped.split('\n')
        return ''.join(_clean_json_lines(lines))


def _init_test_suite_index():
    with _lock:
        if len(_test_identifier_suite_identifiers) == 0:
            test_files = resource_listdir('integrationtest.resources', "p2_test_scenarios")
            for f in test_files:
                if not f.startswith('__') and not f.startswith('.'):
                    test_suite_name = f.replace('.json', '')

                    test_names = [k['scenarioIdentifier'] for k in
                                  json.loads(
                                      _clean_json_str(resource_string('integrationtest.resources.p2_test_scenarios',
                                                                      test_suite_name + '.json')))
                                  ]

                    for i in range(len(test_names)):
                        test_name = test_names[i]
                        assert test_name not in _test_identifier_suite_identifiers, \
                            'Duplicate test name "' + test_name + '"!'
                        _test_identifier_suite_identifiers[test_name] = test_suite_name


def get_parent_test_suite_identifier(test_identifier: str) -> str:
    _init_test_suite_index()
    if test_identifier in _test_identifier_suite_identifiers:
        return _test_identifier_suite_identifiers[test_identifier]
    else:
        for ti in _test_identifier_suite_identifiers:
            if _test_identifier_suite_identifiers[ti] == test_identifier:
                return test_identifier

    raise Exception('No test identifier found!')


def get_test_suite(suite_identifier: str) -> Dict:
    return json.loads(_clean_json_str(resource_string('integrationtest.resources.p2_test_scenarios',
                                                      suite_identifier + '.json')))


def get_test(test_identifier: str) -> Dict:
    suite_identifier = get_parent_test_suite_identifier(test_identifier)
    test_suite = get_test_suite(suite_identifier)
    return [k for k in test_suite if k['scenarioIdentifier'] == test_identifier][0]


def get_test_and_test_suite_list() -> List[str]:
    rval = list()
    _init_test_suite_index()

    for test_identifier in _test_identifier_suite_identifiers:
        rval.append(test_identifier)
        suite_identifier = _test_identifier_suite_identifiers[test_identifier]
        if suite_identifier not in rval:
            rval.append(suite_identifier)

    return rval


def load_immortals_configuration() -> Dict:
    return json.loads(_clean_json_str(resource_string('integrationtest.resources', 'immortals_config.json')))
