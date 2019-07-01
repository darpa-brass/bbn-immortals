#!/usr/bin/env python3

import argparse
import json
import os
from typing import Dict

from odb.datatypes import Scenario, get_scenarios

_parser = None
_subparsers = None

DEFAULT_ROOT_PASSWORD = 'g21534bn890cf57b23n405f987vnb23dh789'

try:
    scenarios = get_scenarios()  # type: Dict[str, Scenario]]
    scenario_names = ','.join(scenarios.keys())
except:
    scenarios = None  # type: None


def init_parser(parent_parser=None):
    global _parser, _subparsers

    if parent_parser is None:
        _parser = argparse.ArgumentParser('IMMoRTALS OrientDB/MDL Helper', add_help=True)
    else:
        _parser = parent_parser.add_parser('odb', help='IMMoRTALS OrientDB/MDL Helper')

    _parser.add_argument('--root-password', type=str,
                         help='The orientdb root password to use instead of the default.')

    _parser.add_argument('--host', type=str,
                         help='If provided, a connection will be made to this host instead of starting a new one.')

    _parser.add_argument('--port', type=int,
                         help='The port to connect to the ODB server on.')

    if scenarios is not None:
        _parser.add_argument('--load-scenario', action='append', type=str,
                             help='If provided, the  database for the specified scenario will be deleted if it ' +
                                  'exists and created. Default values if configuration file not provided: ' +
                                  '{' + ','.join(scenarios.keys()) + '}')

    _parser.add_argument('--input-scenario-json', type=str,
                         help='The input scenario details to start adaptation with')

    _parser.add_argument('--scenario-configuration-file', type=str,
                         help='The path to the configuration file to get the scenario definitions from')

    _parser.add_argument('--start', action='store_true',
                         help='If supplied, the server will be started via this script.')


def main(parser_args):
    global scenarios
    from odb.odbstarter import OdbStarter

    if parser_args.root_password is not None:
        orientdb_root_password = parser_args.root_password

    elif 'ORIENTDB_ROOT_PASSWORD' in os.environ:
        orientdb_root_password = os.environ['ORIENTDB_ROOT_PASSWORD']

    else:
        orientdb_root_password = DEFAULT_ROOT_PASSWORD

    host = '127.0.0.1' if parser_args.host is None else parser_args.host
    port = 2424 if parser_args.port is None else parser_args.port

    starter = None

    if parser_args.start:
        if host != '127.0.0.1' and host != '0.0.0.0' and host != 'localhost':
            raise Exception("When starting the server the host must be '127.0.0.1', '0.0.0.0', or 'localhost'!")

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

        starter = OdbStarter(host, port, orientdb_home, orientdb_root_password)

        if parser_args.host is None:
            starter.start_db()

        display_msg = ('OrientDB is now ready for use. Details:\n' +
                       '\tHost: ' + host + '\n' +
                       '\tPort: ' + str(port) + '\n' +
                       '\tWebsite: http://' + host + ':2480\n' +
                       '\tDatabases:\n'
                       )

    else:
        display_msg = ('The existing OrientDB server has had new databases added to it. Details:\n' +
                       '\tHost: ' + host + '\n' +
                       '\tPort: ' + str(port) + '\n' +
                       '\tWebsite: http://' + host + ':2480\n' +
                       '\tDatabases:\n'
                       )

    from odb.brass_api_helper import BrassApiHelper
    apihelper = BrassApiHelper('root', orientdb_root_password, host, port)

    if parser_args.input_scenario_json is not None:
        scenario_dict = json.loads(parser_args.input_scenario_json)
        scenario_value = Scenario(**scenario_dict)
        apihelper.init_test_scenarios([scenario_value])

    if parser_args.scenario_configuration_file is not None:
        scenarios = get_scenarios(parser_args.scenario_configuration_file)

    if parser_args.load_scenario is not None:
        for scenario_identifier in parser_args.load_scenario:
            scenario = scenarios[scenario_identifier]
            display_msg = \
                display_msg + '\t\t' + 'remote:' + host + ':' + str(port) + '/' + scenario.dbName + '\n'

        apihelper.init_test_scenarios(list(map(lambda x: scenarios[x], parser_args.load_scenario)))

    if parser_args.start:
        display_msg = display_msg + "Press Ctrl-C to halt the OrientDB instance."
        try:
            print(display_msg)
            starter.wait()
        except KeyboardInterrupt:
            pass

        finally:
            if starter is not None:
                starter.stop()

    if parser_args.input_scenario_json is None and parser_args.load_scenario is None and not parser_args.start:
        _parser.print_help()


if __name__ == '__main__':
    init_parser()
    main(_parser.parse_args())
