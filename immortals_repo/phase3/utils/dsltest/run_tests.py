#!/usr/bin/env python
import argparse
import os
import subprocess
import time

SD = os.path.dirname(os.path.realpath(__file__))

JAR_FILE = None
DSL_DIR = None
RULES_FILE = None

default_test_scenarios = {
    "s5": {
        "request_file": "scenarios/s5/dsl-swap-request.json",
        "inventory_file": "scenarios/s5/dsl-swap-inventory.json"
    },
    "s5e1": {
        "request_file": "scenarios/s5e1/dsl-swap-request.json",
        "inventory_file": "scenarios/s5e1/dsl-swap-inventory.json"
    },
    "s5e2": {
        "request_file": "scenarios/s5e2/dsl-swap-request.json",
        "inventory_file": "scenarios/s5e2/dsl-swap-inventory.json"
    },
    "s5e3": {
        "request_file": "scenarios/s5e3/dsl-swap-request.json",
        "inventory_file": "scenarios/s5e3/dsl-swap-inventory.json"
    },
    "s5e4": {
        "request_file": "scenarios/s5e4/dsl-swap-request.json",
        "inventory_file": "scenarios/s5e4/dsl-swap-inventory.json"
    },
    "s5e5": {
        "request_file": "scenarios/s5e5/dsl-swap-request.json",
        "inventory_file": "scenarios/s5e5/dsl-swap-inventory.json"
    },
    "s5e6": {
        "request_file": "scenarios/s5e6/dsl-swap-request.json",
        "inventory_file": "scenarios/s5e6/dsl-swap-inventory.json"
    }
}

debug_test_scenarios = {
    "RP0IP0": {
        "request_file": "input-RP0.json",
        "inventory_file": "inventory-IP0.json"
    },
    "RP1IP0": {
        "request_file": "input-RP1.json",
        "inventory_file": "inventory-IP0.json"
    },
    "RP0IP1": {
        "request_file": "input-RP0.json",
        "inventory_file": "inventory-IP1.json"
    },
    "RP1IP1": {
        "request_file": "input-RP1.json",
        "inventory_file": "inventory-IP1.json"
    },

    "RP0IP0IP1": {
        "request_file": "input-RP0.json",
        "inventory_file": "inventory-IP0IP1.json"
    },
    "RP1IP0IP1": {
        "request_file": "input-RP1.json",
        "inventory_file": "inventory-IP0IP1.json"
    },

    "RP0IP0IP1-TwoDaus": {
        "request_file": "input-RP0.json",
        "inventory_file": "inventory-IP0IP1-twoDaus.json"
    },
    "RP1IP0IP1-twoDaus": {
        "request_file": "input-RP1.json",
        "inventory_file": "inventory-IP0IP1-twoDaus.json"
    },
    "RP0RP1IP0IP1": {
        "request_file": "input-RP0RP1.json",
        "inventory_file": "inventory-IP0IP1.json"
    },
    "RP0RP1IP0IP1-twoDaus": {
        "request_file": "input-RP0RP1.json",
        "inventory_file": "inventory-IP0IP1-twoDaus.json"
    },
    "DSLInterchangeFormat": {
        "request_file": "DSLInterchangeFormat-dsl-input.json",
        "inventory_file": "DSLInterchangeFormat-dsl-dauinventory.json"
    },
    "s5e2-bus-only": {
        "request_file": "s5e2/dsl-swap-request-bus.json",
        "inventory_file": "s5e2/dsl-swap-inventory.json"
    },
    "s5e2-no-signalconditioners": {
        "request_file": "s5e2/dsl-swap-request-nosignalconditioners.json",
        "inventory_file": "s5e2/dsl-swap-inventory.json"
    },
    "s5e2-signalconditioners": {
        "request_file": "s5e2/dsl-swap-request-signalconditioners.json",
        "inventory_file": "s5e2/dsl-swap-inventory.json"
    },
    "s5MoreMeasurements": {
        "request_file": "scenarios/s5MoreMeasurements/dsl-swap-request.json",
        "inventory_file": "scenarios/s5MoreMeasurements/dsl-swap-inventory.json"
    },
    "freeze": {
        "request_file": "freeze/dsl-swap-request.json",
        "inventory_file": "freeze/dsl-swap-inventory.json"
    }
}
scenario_choices = default_test_scenarios.keys() + debug_test_scenarios.keys()

parser = argparse.ArgumentParser('DSL Tester', add_help=True)
parser.add_argument('--scenario', type=str, choices=scenario_choices, help='The scenario to execute')
parser.add_argument('--simple-solver', '-s', action='store_true', help='Use the simple solver')
parser.add_argument('--use-jar-with-dsl', '-j', action='store_true', help='Use the Jar if the DSL is used')
parser.add_argument('--keep-running-on-failure', action='store_true', help='Keep running the tests on failure')


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
    request_file = os.path.realpath(os.path.join(SD, request_file))
    inventory_file = os.path.realpath(os.path.join(SD, inventory_file))
    if not os.path.exists(request_file):
        raise Exception('The file "' + request_file + "' does not exist!")
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
    global JAR_FILE, DSL_DIR, RULES_FILE, default_test_scenarios, debug_test_scenarios
    args = parser.parse_args()

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

    if args.scenario is None:
        for name in default_test_scenarios.keys():
            input_group = default_test_scenarios[name]
            run_test(simple_solver=use_simple_solver, use_jar=use_full_jar,
                     abort_on_failure=(not args.keep_running_on_failure), **input_group)

        for name in debug_test_scenarios.keys():
            input_group = debug_test_scenarios[name]
            run_test(simple_solver=use_simple_solver, use_jar=use_full_jar,
                     abort_on_failure=(not args.keep_running_on_failure), **input_group)

    else:
        if args.scenario in default_test_scenarios:
            input_group = default_test_scenarios[args.scenario]
        elif args.scenario in debug_test_scenarios:
            input_group = debug_test_scenarios[args.scenario]
        else:
            print("Invalid scenario '" + args.scenario + "'!")
            exit(-1)

        run_test(simple_solver=use_simple_solver, use_jar=use_full_jar,
                 abort_on_failure=(not args.keep_running_on_failure), **input_group)


if __name__ == '__main__':
    main()
