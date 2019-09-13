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
    for key in staging_scenarios:
        regression_scenarios[key] = staging_scenarios[key]
    debug_scenarios = test_scenario_data['debug_scenarios']  # type: dict

scenario_choices = sorted(list(regression_scenarios.keys()) + list(debug_scenarios.keys()))

parser = argparse.ArgumentParser('DSL Tester', add_help=True)
parser.add_argument('--scenario', type=str, choices=scenario_choices, help='The scenario to execute')
parser.add_argument('--simple-solver', '-s', action='store_true', help='Use the simple solver')
parser.add_argument('--use-jar-with-dsl', '-j', action='store_true', help='Use the Jar if the DSL is used')
parser.add_argument('--keep-running-on-failure', action='store_true', help='Keep running the tests on failure')
parser.add_argument('--input-inventory', '-i', type=str, help='The input inventory to use')
parser.add_argument('--input-request', '-r', type=str, help='The input request to use')


def exec_cmd(cmd, cwd=None, abort_on_failure=True):
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
    if return_code == 0:
        print('PASS')
        print('============================================================\n\n')

    else:
        print('############################FAIL############################')
        print('############################FAIL############################')
        print('############################FAIL############################')
        print('############################FAIL############################')
        print('RETURNCODE=' + str(return_code))
        print('############################FAIL############################')
        print('############################FAIL############################')
        print('############################FAIL############################')
        print('############################FAIL############################')
        print('------------------------------------------------------------\n\n')
        if abort_on_failure:
            exit(return_code)


def run_test(request_file, inventory_file, simple_solver=False, use_jar=True, abort_on_failure=True):
    """
    :type request_file str
    :type inventory_file str
    :type simple_solver bool
    :type use_jar bool
    :type abort_on_failure bool
    :return:
    """

    global JAR_FILE

    if os.path.exists(request_file):
        request_file = os.path.abspath(request_file)
    else:
        request_file = os.path.realpath(os.path.join(SD, request_file))
        if not os.path.exists(request_file):
            raise Exception('The file "' + request_file + "' does not exist!")

    if os.path.exists(inventory_file):
        inventory_file = os.path.abspath(inventory_file)
    else:
        inventory_file = os.path.realpath(os.path.join(SD, inventory_file))
        if not os.path.exists(inventory_file):
            raise Exception('The file "' + inventory_file + "' does not exist!")

    if use_jar:
        cmd = ['java', '-jar', JAR_FILE,
               '--json-request-path', request_file,
               '--json-inventory-path', inventory_file]

        if simple_solver:
            cmd.append('--simple-solver')
            exec_cmd(cmd, None, abort_on_failure)
        else:
            exec_cmd(cmd, None, abort_on_failure)

    else:
        if simple_solver:
            raise Exception('The SimpleSolver can only be used through the jar!')
        else:
            cmd = ['stack', 'exec', 'resource-dsl', '--', 'swap-dau', '--run',
                   '--rules-file', RULES_FILE,
                   '--inventory-file', inventory_file,
                   '--request-file', request_file]

            exec_cmd(cmd, DSL_DIR, abort_on_failure)


def main():
    global JAR_FILE, DSL_DIR, RULES_FILE, regression_scenarios, debug_scenarios
    args = parser.parse_args()

    if args.input_inventory is not None or args.input_request is not None:
        if args.input_inventory is None:
            raise Exception("Cannot provide an input request without an input inventory!")
            exit(1)
        elif args.input_request is None:
            raise Exception("Cannot provide an input inventory without an input request!")
            exit(1)
        elif args.scenario is not None:
            raise Exception("Cannot provide an input inventory and input request along with an input scenario!")
            exit(1)

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
                 abort_on_failure=(not args.keep_running_on_failure))

    elif args.scenario is None:
        for name in regression_scenarios.keys():
            input_group = regression_scenarios[name]
            if 'request_file_hash' in input_group:
                input_group.pop('request_file_hash')
            if 'inventory_file_hash' in input_group:
                input_group.pop('inventory_file_hash')
            run_test(simple_solver=use_simple_solver, use_jar=use_full_jar,
                     abort_on_failure=(not args.keep_running_on_failure), **input_group)

        for name in debug_scenarios.keys():
            input_group = debug_scenarios[name]
            if 'request_file_hash' in input_group:
                input_group.pop('request_file_hash')
            if 'inventory_file_hash' in input_group:
                input_group.pop('inventory_file_hash')
            run_test(simple_solver=use_simple_solver, use_jar=use_full_jar,
                     abort_on_failure=(not args.keep_running_on_failure), **input_group)

    else:
        if args.scenario in regression_scenarios:
            scenario = regression_scenarios[args.scenario]
            if 'request_file_hash' in scenario:
                scenario.pop('request_file_hash')
            if 'inventory_file_hash' in scenario:
                scenario.pop('inventory_file_hash')

            run_test(simple_solver=use_simple_solver, use_jar=use_full_jar,
                     abort_on_failure=(not args.keep_running_on_failure), **scenario)

        elif args.scenario in debug_scenarios:
            run_test(simple_solver=use_simple_solver, use_jar=use_full_jar,
                     abort_on_failure=(not args.keep_running_on_failure), **scenario)

        else:
            print("Invalid scenario '" + args.scenario + "'!")
            exit(-1)


if __name__ == '__main__':
    main()
