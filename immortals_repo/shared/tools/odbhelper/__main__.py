#!/usr/bin/env python3

import argparse
import os

from odbhelper.datatypes import GraphDetails

_parser = None
_subparsers = None

DEFAULT_ROOT_PASSWORD = 'g21534bn890cf57b23n405f987vnb23dh789'


def init_parser(parent_parser=None):
    global _parser, _subparsers

    if parent_parser is None:
        _parser = argparse.ArgumentParser('IMMoRTALS OrientDB/MDL Helper', add_help=True)
    else:
        _parser = parent_parser.add_parser('odbhelper', help='IMMoRTALS OrientDB/MDL Helper')

    _subparsers = _parser.add_subparsers(dest='selected_parser')

    start_parser = _subparsers.add_parser("start")

    # _parser.add_argument('--start-orientdb', '-s', action='store_true', help='Start the local OrientDB Server')

    s_choices = list(GraphDetails.__dict__['_member_names_'])

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

    start_parser.add_argument('--persistence-only', '-p', action='store_true',
                              help='Only starts the persistence server')

    start_parser.add_argument('--use-default-root-password', action='store_true',
                              help='Uses a default root password for the OrientDB instance')


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

    host = '127.0.0.1'
    port = 2424

    if parser_args.selected_parser == 'start':
        starter = None

        try:
            if parser_args.persistence_only:
                starter = OdbStarter(host, port, orientdb_home, orientdb_root_password)
                starter.start_db(True)
                starter.init_bbn_persistent(parser_args.reset_persistent_db)
                print("The Persistence DB is now running. Press Ctrl-C to halt it.")
                starter.wait()

            else:

                starter = OdbStarter(host, port, orientdb_home, orientdb_root_password)

                starter.start_db(parser_args.unique_log)

                display_msg = ('OrientDB is now ready for use. Details:\n' +
                               '\tHost: ' + host + '\n' +
                               '\tPort: ' + str(port) + '\n' +
                               '\tWebsite: http://' + host + ':2480\n' +
                               '\tDatabases:\n' +
                               '\t\t' + 'remote:' + host + ':' + str(port) + '/' + GraphDetails.s5.db_name + '\n' +
                               '\t\t' + 'remote:' + host + ':' + str(port) + '/' + GraphDetails.s6a.db_name + '\n' +
                               '\t\t' + 'remote:' + host + ':' + str(port) + '/' + GraphDetails.s6b.db_name + '\n' +
                               '\t\t' + 'remote:' + host + ':' + str(port) + '/' + 'BBNPersistent\n'
                               )

                starter.init_bbn_persistent(parser_args.reset_persistent_db)

                if parser_args.reset_scenario is not None:
                    starter.init_test_scenarios(list(map(lambda x: GraphDetails[x], parser_args.reset_scenario)))
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
