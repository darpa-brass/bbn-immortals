#!/usr/bin/env python3

import argparse
import os
import subprocess
import time
from enum import Enum
from typing import Set

_parser = None
_subparsers = None

SCRIPT_DIRECTORY = os.path.dirname(os.path.realpath(__file__))

SWRI_CHALLENGE_PROBLEMS_HOME = os.path.abspath(os.path.join(SCRIPT_DIRECTORY, '../../../swri/challenge-problems/'))
SWRI_ADAPTIVE_CONSTRAINT_SATISFACTION_HOME = os.path.abspath(os.path.join(SCRIPT_DIRECTORY, '../../../swri/adaptive-constraint-satisfaction'))

ORIENTDB_DIR = os.path.join(os.environ['HOME'], '.immortals/orientdb/')

XML_ORIENTDB_LOAD_SCRIPT = os.path.join(SWRI_ADAPTIVE_CONSTRAINT_SATISFACTION_HOME, 'examples/import_export_mdl.py')
XML_ORIENTDB_CONFIG_FILE = os.path.join(SWRI_ADAPTIVE_CONSTRAINT_SATISFACTION_HOME, 'config.json')


class Database(Enum):
    before_swri = (
        'BRASS_Scenario5_BeforeAdaptation-swri',
        os.path.join(SCRIPT_DIRECTORY, '../../../swri/challenge-problems/Scenarios/FlightTesting/Scenario_5/BRASS_Scenario5_BeforeAdaptation.xml'))
    before = (
        'BRASS_Scenario5_BeforeAdaptation',
        os.path.join(SCRIPT_DIRECTORY, '../../phase3/flighttest-constraint-solver/src/main/resources/dummy_data/BRASS_Scenario5_BeforeAdaptation.xml'))
    after = (
        'BRASS_Scenario5_AfterAdaptation',
        os.path.join(SCRIPT_DIRECTORY, '../../phase3/flighttest-constraint-solver/src/main/resources/dummy_data/BRASS_Scenario5_AfterAdaptation.xml'))
    inventory = (
        'TestArticleNetworkInventory',
        os.path.abspath(os.path.join(SWRI_CHALLENGE_PROBLEMS_HOME, '../TestArticleNetworkInventory.xml')))
    other = (
        "OtherGraph",
        os.path.join(SCRIPT_DIRECTORY, '../../phase3/flighttest-constraint-solver/src/main/resources/dummy_data/BRASS_Scenario5_BeforeAdaptation.xml'))

    def __init__(self, identifier, xml_filepath):
        self.identifier = identifier
        self.xml_filepath = xml_filepath


_databases_to_reset = set()  # type: Set[Database]


def init_parser(parent_parser=None):
    global _parser, _subparsers

    if parent_parser is None:
        _parser = argparse.ArgumentParser('IMMoRTALS OrientDB/MDL Helper', add_help=True)
    else:
        _parser = parent_parser.add_parser('integrationtest', help='IMMoRTALS OrientDB/MDL Helper')

    db_choices = list(Database.__dict__['_member_names_'])
    db_choices.append('all')

    _parser.add_argument('--reset', '-D', action='append', dest='databases_to_delete',
                         choices=db_choices)

    _parser.add_argument('--start-orientdb', '-s', action='store_true', help='Start the local OrientDB Server')


def main(parser_args):
    print_help = True

    if parser_args.databases_to_delete is not None:
        print_help = False
        dbs = set(parser_args.databases_to_delete)  # type: Set
        if 'all' in dbs:
            dbs.remove('all')
            dbs = dbs.union(Database.__dict__['_member_names_'])

        for db in dbs:
            d = os.path.join(ORIENTDB_DIR, 'databases', Database[db].identifier)
            if os.path.exists(d):
                print('Deleting directory "' + d + '"...')
                proc = subprocess.run(['rm', '-r', d])
                assert proc.returncode == 0

    if parser_args.start_orientdb:
        print_help = False
        if 'ORIENTDB_ROOT_PASSWORD' not in os.environ:
            print('Please set the "ORIENTDB_ROOT_PASSWORD" environment variable to start the database!')
            exit(1)

        orientdb_process = subprocess.Popen(
            ['bash', os.path.join(ORIENTDB_DIR, 'bin/server.sh')], cwd=os.path.join(ORIENTDB_DIR, 'databases'),
            env={
                'ORIENTDB_ROOT_PASSWORD': os.environ['ORIENTDB_ROOT_PASSWORD']
            })

        time.sleep(4)

        for db in Database:
            d = os.path.join(ORIENTDB_DIR, 'databases', db.identifier)
            if not os.path.exists(d):
                proc = subprocess.run([
                    'python3', XML_ORIENTDB_LOAD_SCRIPT, db.identifier,

                    XML_ORIENTDB_CONFIG_FILE, db.xml_filepath],
                    cwd=SWRI_ADAPTIVE_CONSTRAINT_SATISFACTION_HOME)

                if proc.returncode != 0:
                    orientdb_process.kill()
                    assert proc.returncode == 0
                    time.sleep(1)

            print('Database "' + db.identifier + '"Loaded.')

        orientdb_process.wait()

    if print_help:
        _parser.print_help()


if __name__ == '__main__':
    init_parser()
    main(_parser.parse_args())
