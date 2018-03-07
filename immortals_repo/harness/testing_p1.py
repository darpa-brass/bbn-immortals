#!/usr/bin/env python3

import argparse
import logging
import os
import shutil
import time

from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.resources import resourcemanager
from pymmortals.utils import parse_json_file

_parser = argparse.ArgumentParser(prog='./testing', description='IMMoRTALS Testing Utility')
_sub_parsers = _parser.add_subparsers(help='Available Commands')


def _add_llds_parser(subparsers):
    def llds_main(zargs=None):
        if zargs is None:
            zargs = _parser.parse_args()

        from pymmortals.testing.phase1 import ll_dummy_server
        ll_dummy_server.main(zargs)

    epilog = """
    FLOW Options:
    baseline            Executes the baseline validation scenario with the
                            provided deployment model
    challenge           Executes augmentation and validation against the
                            provided deployment model
    all                 Executes the baseline and challenge scenarios with
                            the provided deployment model sequentially

    DEPLOYMENT_MODEL Options:
    baseline            Baseline deployment model
    fail-all            Deployment model where GPS and bandwidth both fail
    fail-gps            Deployment model where GPS fails but bandwidth succeeds
    fail-bandwidth      Deployment model where bandwidth fails but GPS succeeds
    fail-gpstrusted     Deployment model where trusted requirement fails but
                            otherwise succeeds
    custom              Use a custom deployment model
    """

    parser = subparsers.add_parser('llds',
                                   help='IMMORTALS Mock LL TH',
                                   epilog=epilog,
                                   formatter_class=argparse.RawTextHelpFormatter)

    parser.add_argument('flow', metavar='FLOW', choices=['baseline', 'challenge', 'all'])
    parser.add_argument('deployment_model',
                        metavar='DEPLOYMENT_MODEL',
                        choices=['baseline', 'fail-all', 'fail-gps', 'fail-bandwidth', 'fail-gpstrusted', 'custom'])
    parser.add_argument('-f', '--scenario-file', type=str)
    parser.add_argument('-s', '--scenario-string', type=str)
    parser.set_defaults(func=llds_main)


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

            lldsl = os.path.join(get_configuration().immortalsRoot, 'harness/ll_dummy_server.log')
            if os.path.exists(lldsl):
                shutil.move(lldsl,
                            os.path.join(get_configuration().immortalsRoot,
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
        from pymmortals.testing.phase1 import systemvalidator
        systemvalidator.smoke_test(zargs)

    o_parser = subparsers.add_parser('orchestrate', help='Orchestrate an end-to-end scenario')
    o_parser.add_argument('mode',
                          metavar='MODE',
                          choices=resourcemanager.get_p1_test_list())
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
    logging.Logger.setLevel(logging.getLogger(), logging.DEBUG if get_configuration().debugMode else logging.INFO)

    args = _parser.parse_args()

    if args.__contains__('func'):
        args.func(args)
    else:
        _parser.print_help()
