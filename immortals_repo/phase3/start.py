#!/usr/bin/env python3

import os

import argparse
import subprocess
import sys
from typing import List

_parser = argparse.ArgumentParser('IMMoRTALS Scenario 5/Scenario 6 Launcher', add_help=True)

_parser.add_argument('--scenario', action='store', choices=['5', '6'],
                     help='The scenario which will be executed')
_parser.add_argument('--scenario-help', action='store', choices=['5', '6'],
                     help='Calls "--help" on the specified scenario')
_parser.add_argument('--odb-url', action='store', type=str,
                     help='The URL of the evaluation OrientDB server. Example: \n' +
                          '"remote:OrientDB.example.com:2424/GratefulDeadConcerts"')
_parser.add_argument('--persistence-url', action='store', type=str,
                     help="The URL of the persistence OrientDB server.")
_parser.add_argument('--local-test', action='append', choices=['s5', 's6a', 's6b', 'all'],
                     help="Starts a (properly set up) local OrientDB server and runs through some sanity tests.")
_parser.add_argument('--odb-user', action='store', type=str, default='admin',
                     help='The user for the evaluation OrientDB server. The default is "admin"')
_parser.add_argument('--odb-password', action='store', type=str, default='admin',
                     help='The user password for the evaluation OrientDB server. Defaults to "admin"')
_parser.add_argument('--start-persistence-server', action='store_true',
                     help='Starts the persistence server for logging data produced by evaluation sessions')

_scenario_5_cmd = ['java', '-jar', 'flighttest-constraint-solver/flighttest-constraint-solver.jar']
_scenario_6_cmd = [
    'java', '-DevalType=live',
    '-DcannedInputJson=../knowledge-repo/cp/cp3.1/cp-eval-service/examples/sanityCheck.json',
    '-DpathToXsdTranslationServicePy=../knowledge-repo/cp/cp3.1/xsd-tranlsation-service-aql/aql/server.py',
    '-DxsdTranslationServicePort=8090',
    '-DpythonExecutable=python',
    '-DfusekiHome=' + os.environ['FUSEKI_HOME'],
    '-Dserver.port=8088',
    '-DessTemplateDir=../knowledge-repo/cp/cp3.1/cp-ess-min',
    '-DpathToInstrumentationJar=../knowledge-repo/cp/cp3.1/etc/rampart.jar',
    '-DdomainKnowledge=../knowledge-repo/cp/cp3.1/cp-ess-min/etc/arch.ttl',
    '-jar', 'cp3.1-eval-service.jar']


def exec_scenario_5(env_values, argv: List[str]) -> bool:
    cmd = list(_scenario_5_cmd)

    p = subprocess.run(cmd + argv, env=env_values, stdout=sys.stdout, stderr=sys.stderr)
    return p.returncode == 0


def exec_scenario5_help() -> str:
    cmd = list(_scenario_5_cmd) + ['--help']
    p = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    return p.stdout.decode()


def exec_scenario_6(env_values, argv: List[str], be_exceptional: bool = True) -> bool:
    # copy the eval harness JAR into .
    cmd = ['cp', '../knowledge-repo/cp/cp3.1/cp-eval-service/target/immortals-cp3.1-eval-service-boot.jar',
           'cp3.1-eval-service.jar']
    p = subprocess.run(cmd, env=env_values, stderr=sys.stderr, stdout=sys.stdout)

    if p.returncode != 0:
        if be_exceptional:
            raise Exception("Could not copy Scenario 6 jar into execution directory!")
        else:
            print("Could not copy Scenario 6 jar into execution directory!")
            return False

    # launch the executable JAR with the configuration retrieved from OrientDB
    cmd = list(_scenario_6_cmd)

    p = subprocess.run(cmd + argv, env=env_values, stdout=sys.stdout, stderr=sys.stderr)
    return p.returncode == 0


def exec_scenario6_help() -> str:
    cmd = ['cp', '../knowledge-repo/cp/cp3.1/cp-eval-service/target/immortals-cp3.1-eval-service-boot.jar',
           'cp3.1-eval-service.jar']
    p = subprocess.run(cmd, stderr=sys.stderr, stdout=sys.stdout)

    if p.returncode != 0:
        print("Could not copy Scenario 6 jar into execution directory!")
    else:
        cmd = list(_scenario_6_cmd) + ['--help']
        p = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return p.stdout.decode()


def main():
    args, pass_through_args = _parser.parse_known_args()

    if args.start_persistence_server:
        subprocess.run(
            ['bash', '../shared/tools.sh', 'odbhelper', 'start', '--persistence-only', '--use-default-root-password'])

    elif args.scenario_help is not None:
        if args.scenario_help == '5':
            print(exec_scenario5_help())
        elif args.scenario_help == '6':
            print(exec_scenario6_help())

    elif args.local_test is not None and len(args.local_test) > 0:
        test_scenario_results = []
        test_scenarios = []
        if 's5' in args.local_test or 'all' in args.local_test:
            test_scenarios.append(['5', 'remote:127.0.0.1:2424/IMMORTALS_TEST-SCENARIO_5'])

        if 's6a' in args.local_test or 'all' in args.local_test:
            test_scenarios.append(['6', 'remote:127.0.0.1:2424/IMMORTALS_TEST-SCENARIO_6-UNKNOWN_SCHEMA'])

        if 's6b' in args.local_test or 'all' in args.local_test:
            test_scenarios.append(['6', 'remote:127.0.0.1:2424/IMMORTALS_TEST-SCENARIO_6-KNOWN_SCHEMA'])

        for config in test_scenarios:
            env_values = os.environ.copy()
            env_values['ORIENTDB_PERSISTENCE_TARGET'] = 'remote:127.0.0.1:2424/BBNPersistent'
            env_values['ORIENTDB_EVAL_TARGET'] = config[1]
            if config[0] == '5':
                passed = exec_scenario_5(env_values, pass_through_args)

            elif config[0] == '6':
                passed = exec_scenario_6(env_values, pass_through_args)

            else:
                raise Exception("Unexpected scenario '" + config[0] + "'!")

            test_scenario_results.append([config, passed])

        failure_result = False

        for results in test_scenario_results:
            scenario_identifier = results[0][0]
            orientdb_graph = results[0][1]
            passed = results[1]

            if passed:
                print('Scenario ' + scenario_identifier + ' using graph ' + orientdb_graph + ' PASSED')
            else:
                print('Scenario ' + scenario_identifier + ' using graph ' + orientdb_graph + ' FAILED')
                failure_result = True

        if failure_result:
            exit(1)

    else:
        env_values = os.environ.copy()

        if args.scenario is None:
            _parser.print_usage()
            print('IMMoRTALS Scenario 5/Scenario 6 Launcher: error: the following arguments are required: --scenario')
            exit(1)

        if args.odb_url is None:
            _parser.print_usage()
            print('IMMoRTALS Scenario 5/Scenario 6 Launcher: error: the following arguments are required: --odb-url')
            exit(1)

        if args.persistence_url is None:
            _parser.print_usage()
            print(
                'IMMoRTALS Scenario 5/Scenario 6 Launcher: error: ' +
                'the following arguments are required: --persistence-url')
            exit(1)

        env_values['ORIENTDB_EVAL_TARGET'] = args.odb_url
        env_values['ORIENTDB_PERSISTENCE_TARGET'] = args.persistence_url
        if args.odb_user is not None:
            env_values['ORIENTDB_EVAL_USER'] = args.odb_user
        if args.odb_password is not None:
            env_values['ORIENTDB_EVAL_PASSWORD'] = args.odb_password

        if args.scenario == '5':
            exec_scenario_5(env_values, pass_through_args)

        elif args.scenario == '6':
            exec_scenario_6(env_values, pass_through_args)

        else:
            print('Invalid scenario "' + args.scenario + '"!')
            sys.exit(1)


if __name__ == '__main__':
    main()
