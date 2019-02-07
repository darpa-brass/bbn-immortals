#!/usr/bin/env python3

import argparse
import os

from odbhelper.datatypes import TestScenario

_parser = None
_subparsers = None


def init_parser(parent_parser=None):
    global _parser, _subparsers

    if parent_parser is None:
        _parser = argparse.ArgumentParser('IMMoRTALS OrientDB/MDL Helper', add_help=True)
    else:
        _parser = parent_parser.add_parser('odbhelper', help='IMMoRTALS OrientDB/MDL Helper')

    _subparsers = _parser.add_subparsers(dest='selected_parser')

    start_parser = _subparsers.add_parser("start")

    # _parser.add_argument('--start-orientdb', '-s', action='store_true', help='Start the local OrientDB Server')

    s_choices = list(TestScenario.__dict__['_member_names_'])

    start_parser.add_argument('--reset-scenario', '-r', action='append', choices=s_choices,
                              help='If provided, the  database for the specified scenario will be deleted ' +
                                   ' and recreated.')

    # start_parser.add_argument('--test-scenario', '-t', action='store', choices=s_choices,
    #                           help='Preload test scenario data.')
    start_parser.add_argument('--unique-log', '-u', action='store_true',
                              help='If true, timestamped log files will be created instead of replacing the existing ' +
                                   'immortals-orientdb-stderr.log and immortals-orientdb-stdout.log,')

    start_parser.add_argument('--reset-persistent-db', '-R', action='store_true',
                              help='If true, the persistent DB will be reset.')


def main(parser_args):
    from odbhelper.odbstarter import OdbStarter

    if 'ORIENTDB_HOME' not in os.environ:
        print('Please set the "ORIENTDB_HOME" environment variable to your OrientDB instance root!')
        exit(1)

    if 'ORIENTDB_ROOT_PASSWORD' not in os.environ:
        print('Please set the "ORIENTDB_ROOT_PASSWORD" environment variable to your OrientDB root password! If you have not started OrientDB yet this password will be used to initialize it!')
        exit(1)

    host = '127.0.0.1'
    port = 2424
    orientdb_home = os.environ['ORIENTDB_HOME']
    orientdb_root_password = os.environ['ORIENTDB_ROOT_PASSWORD']

    starter = None

    if parser_args.selected_parser == 'start':

        try:
            starter = OdbStarter(host, port, orientdb_home, orientdb_root_password)

            starter.start_db(parser_args.unique_log)

            display_msg = ('OrientDB is now ready for use. Details:\n' +
                           '\tHost: ' + host + '\n' +
                           '\tPort: ' + str(port) + '\n' +
                           '\tWebsite: http://' + host + ':2480\n' +
                           '\tDatabases:\n' +
                           '\t\t' + 'remote:' + host + ':' + str(port) + '/' + TestScenario.s5.db_name + '\n' +
                           '\t\t' + 'remote:' + host + ':' + str(port) + '/' + TestScenario.s6a.db_name + '\n' +
                           '\t\t' + 'remote:' + host + ':' + str(port) + '/' + TestScenario.s6b.db_name + '\n' +
                           '\t\t' + 'remote:' + host + ':' + str(port) + '/' + 'BBNPersistent\n'
                           )

            starter.init_bbn_persistent(parser_args.reset_persistent_db)

            if parser_args.reset_scenario is not None:
                starter.init_test_scenarios(list(map(lambda x: TestScenario[x], parser_args.reset_scenario)))
            else:
                starter.init_test_scenarios()

            display_msg = \
                display_msg + "Press Ctrl-C to halt the OrientDB instance."

            print(display_msg)
            starter.wait()

        except KeyboardInterrupt:
            pass

        finally:
            if starter is not None:
                starter.stop()

    else:
        _parser.print_help()


if __name__ == '__main__':
    init_parser()
    main(_parser.parse_args())
