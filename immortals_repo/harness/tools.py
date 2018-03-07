#!/usr/bin/env python3

import argparse
import logging
import os
import shutil
import time

from pymmortals.immortalsglobals import get_configuration
from pymmortals.immortalsglobals import main_thread_cleanup_hookup
from pymmortals.resources import resourcemanager
from pymmortals.testing.systemvalidator import SystemValidator
from pymmortals.utils import parse_json_file

_parser = argparse.ArgumentParser(prog='./tools.sh', description='IMMoRTALS Testing Utility')
_parser.add_argument('-d', '--debug', action='store_true', help="Enable Debug Logging")
_sub_parsers = _parser.add_subparsers(help='Available Commands')


def _add_llds_parser(subparsers):
    def llds_main(zargs=None):
        if zargs is None:
            zargs = _parser.parse_args()

        from pymmortals.testing import ll_dummy_server
        ll_dummy_server.run_test_scenario(test_suite_identifier=zargs.test_suite,
                                          test_identifier=zargs.test)

    llds_parser = subparsers.add_parser('llds',help='Immortals Mock LL TH')
    suite_parsers = llds_parser.add_subparsers(help='Avaiable Test Suites')
    suite_identifiers = resourcemanager.get_p2_test_suite_list()

    for si in suite_identifiers:
        test_identifiers = resourcemanager.get_p2_test_suite_test_list(si)
        suite_parser = suite_parsers.add_parser(si)
        suite_parser.add_argument('test', metavar='TEST', choices=test_identifiers)
        suite_parser.set_defaults(func=llds_main, test_suite=si)


def _add_olympus_parser(subparsers):
    # noinspection PyUnusedLocal
    def olympus_main(zargs=None):
        from pymmortals.olympus import Olympus
        from pymmortals import immortalsglobals
        olympus = Olympus(immortalsglobals.get_event_router())
        olympus.start()

    olympus_parser = subparsers.add_parser('olympus', help='IMMoRTALS Olympus Web Interface')
    olympus_parser.set_defaults(func=olympus_main)


def _add_tools_parser(subparsers):
    def tools_dmttl_main(zargs=None):
        if zargs is None:
            zargs = _parser.parse_args()

        from pymmortals.ttl_bridge import execute_ttl_generation
        from pymmortals.datatypes.deployment_model import LLP1Input
        sc_d = parse_json_file(open(zargs.input_file, 'r'))
        sc_obj = LLP1Input.from_dict(sc_d)
        execute_ttl_generation(sc_obj, zargs.output_file)

    tools_parser = subparsers.add_parser('tools', help='IMMoRTALS Tools')
    _tools_parser_subparsers = tools_parser.add_subparsers(help='Available Utility Commands')
    dmttl_parser = _tools_parser_subparsers.add_parser('dmtottl', help="Deployment Model To TTL")
    dmttl_parser.add_argument('input_file', metavar='INPUT_FILE')
    dmttl_parser.add_argument('output_file', metavar='OUTPUT_FILE')
    dmttl_parser.set_defaults(func=tools_dmttl_main)


def _add_vacuum_parser(subparsers):
    def vacuum_main(zargs=None):
        if zargs is None:
            zargs = _parser.parse_args()

        if zargs.operation == 'files' or zargs.operation == 'all':
            timestamp = str(int(time.time() * 1000))

            if os.path.exists('/test/debug'):
                shutil.move('/test/debug', '/test/debug-' + timestamp)

            if os.path.exists('/test/log'):
                shutil.move('/test/log', '/test/log-' + timestamp)

            lldsl = os.path.join(get_configuration().globals.immortalsRoot, 'harness/ll_dummy_server.log')
            if os.path.exists(lldsl):
                shutil.move(lldsl,
                            os.path.join(get_configuration().globals.immortalsRoot,
                                         'harness/ll_dummy_server' + timestamp + '.log'))

        if zargs.operation == 'emulators' or zargs.operation == 'all':
            from pymmortals.scenariorunner.platforms.android import emuhelper
            emuhelper.wipe_emulators()

    vacuum_parser = subparsers.add_parser('vacuum', help='Vacuum Cleaner')
    vacuum_parser.add_argument('operation',
                               metavar='OPERATION',
                               choices=['all', 'files', 'emulators'])
    vacuum_parser.set_defaults(func=vacuum_main)


def _add_emulator_setup_parser(subparsers):
    # noinspection PyUnusedLocal
    def setup_emulators_main(zargs=None):
        from pymmortals.scenariorunner.platforms.android.emuhelper import initially_setup_emulators
        initially_setup_emulators()

    emulator_parser = subparsers.add_parser('setupemulators')
    emulator_parser.set_defaults(func=setup_emulators_main)


def _add_orchestrate_parser(subparsers):
    def orchestrate_main(zargs=None):
        sv = SystemValidator(immortals_root=zargs.immortalsRoot)
        sv.start(test_suite_identifier=zargs.test_suite, test_identifier=None)

    o_parser = subparsers.add_parser('orchestrate', help='Orchestrate an end-to-end scenario')
    o_parser.add_argument('test_suite',
                          metavar='TEST_SUITE',
                          choices=resourcemanager.get_p2_test_suite_list())
    o_parser.add_argument('-r', '--immortalsRoot', type=str)
    o_parser.set_defaults(func=orchestrate_main)


_add_llds_parser(_sub_parsers)
_add_olympus_parser(_sub_parsers)
_add_tools_parser(_sub_parsers)
_add_vacuum_parser(_sub_parsers)
_add_orchestrate_parser(_sub_parsers)
_add_emulator_setup_parser(_sub_parsers)

network_logger = logging.getLogger

if __name__ == '__main__':
    args = _parser.parse_args()
    logging.Logger.setLevel(logging.getLogger(), logging.DEBUG if args.debug else logging.INFO)

    if args.__contains__('func'):
        main_thread_cleanup_hookup()
        args.func(args)
    else:
        _parser.print_help()
