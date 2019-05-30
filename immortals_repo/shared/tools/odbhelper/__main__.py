#!/usr/bin/env python3

import argparse
import os

from odbhelper import datatypes

_parser = None
_subparsers = None

DEFAULT_ROOT_PASSWORD = 'g21534bn890cf57b23n405f987vnb23dh789'

scenarios = datatypes.get_scenarios()


def init_parser(parent_parser=None):
    global _parser, _subparsers

    if parent_parser is None:
        _parser = argparse.ArgumentParser('IMMoRTALS OrientDB/MDL Helper', add_help=True)
    else:
        _parser = parent_parser.add_parser('odbhelper', help='IMMoRTALS OrientDB/MDL Helper')

    _subparsers = _parser.add_subparsers(dest='selected_parser')

    start_parser = _subparsers.add_parser("start")

    s_choices = scenarios.keys()

    start_parser.add_argument('--reset-scenario', '-r', action='append', choices=s_choices,
                              help='If provided, the  database for the specified scenario will be deleted ' +
                                   ' and recreated.')

    start_parser.add_argument('--unique-log', '-u', action='store_true',
                              help='If true, timestamped log files will be created instead of replacing the existing ' +
                                   'immortals-orientdb-stderr.log and immortals-orientdb-stdout.log,')

    start_parser.add_argument('--reset-persistent-db', '-R', action='store_true',
                              help='If true, the persistent DB will be reset.')

    start_parser.add_argument('--persistence-only', '-p', action='store_true',
                              help='Only starts the persistence server')

    start_parser.add_argument('--use-default-root-password', action='store_true',
                              help='Uses a default root password for the OrientDB instance')

    start_parser.add_argument('--host', type=str,
                              help='If provided, a connection will be made to this host instead of starting a new one.')

    start_parser.add_argument('--port', type=int,
                              help='The port to connect to the ODB server on.')


def main(parser_args):
    from odbhelper.odbstarter import OdbStarter

    if 'ORIENTDB_HOME' in os.environ:
        orientdb_home = os.environ['ORIENTDB_HOME']
        if not os.path.exists(orientdb_home):
            raise Exception('"ORIENTDB_HOME" directory "' + orientdb_home + '" could not be found!')

    elif 'HOME' in os.environ and os.path.exists(os.environ['HOME']) and os.path.exists(
            os.path.join(os.environ['HOME'], '.immortals', 'orientdb')):
        orientdb_home = os.path.join(os.environ['HOME'], '.immortals', 'orientdb')

    else:
        raise Exception(
            'Environment variable "ORIENTDB_HOME" is unset and the default orientdb directory of "' + os.path.join(
                os.environ['HOME'], '.immortals', 'orientdb') + '" does not exist!')

    if parser_args.use_default_root_password:
        orientdb_root_password = DEFAULT_ROOT_PASSWORD

    elif 'ORIENTDB_ROOT_PASSWORD' in os.environ:
        orientdb_root_password = os.environ['ORIENTDB_ROOT_PASSWORD']

    else:
        raise Exception('Please set the "ORIENTDB_ROOT_PASSWORD" environment variable to your OrientDB root ' +
                        'password! If you have not started OrientDB yet this password will be used to initialize it!')

    host = '127.0.0.1' if parser_args.host is None else parser_args.host
    port = 2424 if parser_args.port is None else parser_args.port

    if parser_args.selected_parser == 'start':
        starter = None

        try:
            if parser_args.persistence_only:
                starter = OdbStarter(host, port, orientdb_home, orientdb_root_password)
                if parser_args.host is None:
                    starter.start_db(True)
                starter.init_bbn_persistent(parser_args.reset_persistent_db)
                print("The Persistence DB is now running. Press Ctrl-C to halt it.")
                starter.wait()

            else:

                starter = OdbStarter(host, port, orientdb_home, orientdb_root_password)

                if parser_args.host is None:
                    starter.start_db(parser_args.unique_log)

                display_msg = ('OrientDB is now ready for use. Details:\n' +
                               '\tHost: ' + host + '\n' +
                               '\tPort: ' + str(port) + '\n' +
                               '\tWebsite: http://' + host + ':2480\n' +
                               '\tDatabases:\n'
                               )

                # starter.init_bbn_persistent(parser_args.reset_persistent_db)
                display_msg = display_msg + '\t\t' + 'remote:' + host + ':' + str(port) + '/' + 'BBNPersistent\n'

                if parser_args.reset_scenario is not None:
                    for scenario_identifier in parser_args.reset_scenario:
                        scenario = scenarios[scenario_identifier]
                        display_msg = \
                            display_msg + '\t\t' + 'remote:' + host + ':' + str(port) + '/' + scenario.dbName + '\n'

                    starter.init_test_scenarios(list(map(lambda x: scenarios[x], parser_args.reset_scenario)))

                if parser_args.host is None:
                    display_msg = display_msg + "Press Ctrl-C to halt the OrientDB instance."

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
