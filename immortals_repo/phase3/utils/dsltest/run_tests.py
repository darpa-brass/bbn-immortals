#!/usr/bin/env python
import argparse
import json
import os
import subprocess
import time

SD = os.path.dirname(os.path.realpath(__file__))

JAR_FILE = None
DSL_DIR = None
RULES_FILE = None

with open(os.path.join(SD, 'scenarios/test_scenarios.json'), 'r') as f:
    test_scenario_data = json.load(f)
    regression_scenarios = test_scenario_data['regression_scenarios']  # type: dict
    staging_scenarios = test_scenario_data['staging_scenarios']  # type: dict
    debug_scenarios = test_scenario_data['debug_scenarios']  # type: dict
    bad_scenarios = test_scenario_data['bad_scenarios']  # type: dict

scenario_choices = sorted(list(regression_scenarios.keys()) + list(debug_scenarios.keys()) +
                          list(staging_scenarios) + list(bad_scenarios))

parser = argparse.ArgumentParser('DSL Tester', add_help=True)
parser.add_argument('--scenario', type=str, choices=scenario_choices, help='The scenario to execute')
parser.add_argument('--simple-solver', '-s', action='store_true', help='Use the simple solver')
parser.add_argument('--use-jar-with-dsl', '-j', action='store_true', help='Use the Jar if the DSL is used')
parser.add_argument('--keep-running-on-failure', action='store_true', help='Keep running the tests on failure')
parser.add_argument('--input-inventory', '-i', type=str, help='The input inventory to use')
parser.add_argument('--input-request', '-r', type=str, help='The input request to use')
parser.add_argument('--run-staging-tests', action='store_true', help='Run the staging tests')
parser.add_argument('--run-regression-tests', action='store_true', help='Run the regression tests')
parser.add_argument('--run-debug-tests', action='store_true', help='Run the debug tests')
parser.add_argument('--run-bad-tests', action='store_true', help='Run the tests that are known to be bad')


def exec_cmd(cmd, success_expected, cwd=None, abort_on_failure=True):
    print('============================================================')
    print('--------------------------COMMAND---------------------------')

    idx = 0
    print_line = []
    use_tab = False
    while idx < len(cmd):
        current_line = cmd[idx]
        if ((idx + 1) < len(cmd)
                and current_line.startswith('--') and len(current_line) > 2
                and not cmd[idx + 1].startswith('--')):
            if len(print_line) > 0:
                print(('\t' if use_tab else '') + ' '.join(print_line) + ' \\')
                use_tab = True
                print_line = []
            print(('\t' if use_tab else '') + current_line + ' ' + cmd[idx + 1] + ' \\')
            use_tab = True
            idx = idx + 2
        else:
            print_line.append(current_line)
            idx = idx + 1

    if len(print_line) > 0:
        print(('\t' if use_tab else '') + ' '.join(print_line))

    start_time = time.time()
    print('-----------------------COMMAND OUTPUT-----------------------')
    return_code = subprocess.call(cmd, cwd=cwd)
    print('--------------------------DURATION--------------------------')
    print(str(int(time.time() - start_time)) + ' seconds')
    print('---------------------------RESULT---------------------------')

    if return_code == 0 and success_expected:
        print('PASS')
        print('============================================================\n\n')

    elif return_code != 0 and not success_expected:
        print('PASS (Failure Expected)')
        print('============================================================\n\n')

    else:
        print('############################FAIL############################')
        print('############################FAIL############################')
        print('############################FAIL############################')
        print('############################FAIL############################')
        if success_expected:
            print('RETURNCODE=' + str(return_code))
        else:
            print('RETURNCODE=' + str(return_code) + ' (Failure Expected)')
        print('############################FAIL############################')
        print('############################FAIL############################')
        print('############################FAIL############################')
        print('############################FAIL############################')
        print('------------------------------------------------------------\n\n')
        if abort_on_failure:
            exit(return_code)


def resolve_file(filepath):
    if os.path.exists(filepath):
        filepath = os.path.abspath(filepath)
    else:
        filepath = os.path.realpath(os.path.join(SD, filepath))
        if not os.path.exists(filepath):
            raise Exception('The file "' + filepath + "' does not exist!")

    return filepath


def run_test(request_file, inventory_file, success_expected, simple_solver=False, use_jar=True, abort_on_failure=True):
    """
    :type request_file str
    :type inventory_file str
    :type success_expected bool
    :type simple_solver bool
    :type use_jar bool

    :type abort_on_failure bool
    :return:
    """

    global JAR_FILE

    request_file = resolve_file(request_file)
    inventory_file = resolve_file(inventory_file)

    if use_jar:
        cmd = ['java', '-jar', JAR_FILE,
               '--json-request-path', request_file,
               '--json-inventory-path', inventory_file]

        if simple_solver:
            cmd.append('--simple-solver')
            exec_cmd(cmd, success_expected, None, abort_on_failure)
        else:
            exec_cmd(cmd, success_expected, None, abort_on_failure)

    else:
        if simple_solver:
            raise Exception('The SimpleSolver can only be used through the jar!')
        else:
            cmd = ['stack', 'exec', 'resource-dsl', '--', 'swap-dau', '--run',
                   '--rules-file', RULES_FILE,
                   '--inventory-file', inventory_file,
                   '--request-file', request_file]

            exec_cmd(cmd, success_expected, DSL_DIR, abort_on_failure)


def main():
    global JAR_FILE, DSL_DIR, RULES_FILE, regression_scenarios, debug_scenarios, staging_scenarios, bad_scenarios
    args = parser.parse_args()

    if args.input_inventory is not None or args.input_request is not None:
        if args.input_inventory is None:
            raise Exception("Cannot provide an input request without an input inventory!")
        elif args.input_request is None:
            raise Exception("Cannot provide an input inventory without an input request!")
        elif args.scenario is not None:
            raise Exception("Cannot provide an input inventory and input request along with an input scenario!")
        elif args.run_staging_tests or args.run_regression_tests or args.run_debug_tests:
            raise Exception("Cannot provide and input inventory and input request with specified test suites!")

    total_scenarios = args.run_staging_tests + args.run_regression_tests + args.run_debug_tests + args.run_bad_tests

    if total_scenarios > 1:
        raise Exception(
            'The "--run-staging-tests", "--run-regression-tests", "--run-debug-tests", and "--run-bad-tests"' +
            ' parameters cannot be used with any other parameters!')

    def run_scenario(scenario):
        if 'request_file_hash' in scenario:
            scenario.pop('request_file_hash')
        if 'inventory_file_hash' in scenario:
            scenario.pop('inventory_file_hash')

        run_test(simple_solver=use_simple_solver, use_jar=use_full_jar,
                 abort_on_failure=(not args.keep_running_on_failure), **scenario)

    def run_scenarios(scenario_set):
        for name in scenario_set.keys():
            input_group = scenario_set[name]
            run_scenario(input_group)

    if args.simple_solver:
        use_simple_solver = True
        use_full_jar = True

    else:
        use_simple_solver = False
        use_full_jar = args.use_jar_with_dsl

    if use_full_jar:
        JAR_FILE = os.path.realpath(
            os.path.join(SD, '../../flighttest-constraint-solver/flighttest-constraint-solver.jar'))
        if not os.path.exists(JAR_FILE):
            raise Exception('The solver jar "' + JAR_FILE + '" does not exist!')

    if not use_simple_solver:
        DSL_DIR = os.path.realpath(os.path.join(SD, '../../../dsl/resource-dsl'))
        if not os.path.exists(DSL_DIR):
            raise Exception('The DSL directory "' + DSL_DIR + '" does not exist!')

        RULES_FILE = os.path.realpath(os.path.join(SD, 'swap-rules.json'))
        if not os.path.exists(RULES_FILE):
            raise Exception('The file "' + RULES_FILE + '" does not exist!')

    if args.input_inventory is not None or args.input_request is not None:
        run_test(inventory_file=args.input_inventory, request_file=args.input_request,
                 simple_solver=use_simple_solver, use_jar=use_full_jar,
                 abort_on_failure=(not args.keep_running_on_failure), success_expected=True)

    elif args.run_staging_tests:
        run_scenarios(staging_scenarios)
    elif args.run_regression_tests:
        run_scenarios(regression_scenarios)
    elif args.run_debug_tests:
        run_scenarios(debug_scenarios)
    elif args.run_bad_tests:
        run_scenarios(bad_scenarios)

    elif args.scenario is None:
        run_scenarios(regression_scenarios)
        run_scenarios(debug_scenarios)
        run_scenarios(staging_scenarios)
        run_scenarios(bad_scenarios)

    else:
        if args.scenario in regression_scenarios:
            run_scenario(regression_scenarios[args.scenario])
        elif args.scenario in debug_scenarios:
            run_scenario(debug_scenarios[args.scenario])
        elif args.scenario in bad_scenarios:
            run_scenario(bad_scenarios[args.scenario])
        elif args.scenario in staging_scenarios:
            run_scenario(staging_scenarios[args.scenario])
        else:
            print("Invalid scenario '" + args.scenario + "'!")
            exit(-1)


if __name__ == '__main__':
    main()
