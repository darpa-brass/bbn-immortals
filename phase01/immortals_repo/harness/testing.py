#!/usr/bin/env python2.7

import argparse
import logging
from threading import Thread

_parser = argparse.ArgumentParser(prog='./testing', description='IMMoRTALS Testing Utility')


def llds_add_parser_arguments(psr):
    psr.add_argument('-thp', '--test-harness-port', type=int)
    psr.add_argument('-tha', '--test-harness-address', type=str)
    psr.add_argument('-tap', '--test-adapter-port', type=int)
    psr.add_argument('-taa', '--test-adapter-address', type=str)
    psr.add_argument('flow', metavar='FLOW', choices=['baselineA', 'baselineB', 'challenge', 'all'])
    psr.add_argument('failure', metavar='FAILURE_PROPERTY', choices=['none', 'all', 'gps', 'bandwidth'])
    psr.add_argument('-f', '--scenario-file', type=str)
    psr.add_argument('-s', '--scenario-string', type=str)


def olympus_main(args=None):
    from scenarioconductor.olympus import Olympus
    from tornado.ioloop import IOLoop
    olympus = Olympus('127.0.0.1', 55555)
    t = Thread(target=olympus.start)
    t.setDaemon(True)
    t.start()

    IOLoop.current().start()


def llds_main(args=None):
    from scenarioconductor.testing import ll_dummy_server as llds
    args = _parser.parse_args()
    llds.main(args)


_sub_parsers = _parser.add_subparsers(help='Available Commands')

llds_parser = _sub_parsers.add_parser('llds', help='IMMoRTALS Mock LL TestHarness')

ll_dummy_server_parser = _sub_parsers.add_parser('llds', help='IMMORTALS Mock LL TH')
llds_add_parser_arguments(ll_dummy_server_parser)
ll_dummy_server_parser.set_defaults(func=llds_main)

sc_parser = _sub_parsers.add_parser('sc', help='IMMoRTALS Scenario Conductor')
sc_parser.add_argument('-f', '--sc-configuration-file', type=str)

olympus_parser = _sub_parsers.add_parser('olympus', help='IMMoRTALS Olympus Web Interface')
olympus_parser.set_defaults(func=olympus_main)

network_logger = logging.getLogger


def main():
    args = _parser.parse_args()

    if hasattr(args, 'sc_configuration_file'):
        from scenarioconductor import immortalsglobals as ig
        from scenarioconductor.scenarioconductor import ScenarioConductor
        from scenarioconductor.reporting.testharnessreporter import ConsoleHarnessLogger
        from scenarioconductor.data.base.scenarioapiconfiguration import ScenarioConductorConfiguration
        ig.main_thread_cleanup_hookup()

        # ig.set_logger(FakeTestHarnessReporter(ig.configuration.logRoot, ig.configuration.artifactRoot))
        ig.set_logger(ConsoleHarnessLogger(ig.configuration.logRoot, ig.configuration.artifactRoot))

        scc_d = {
            "server": {
                "bandwidth": 1000
            },
            "clients": [
                {
                    "imageBroadcastIntervalMS": "10000",
                    "latestSABroadcastIntervalMS": "1000",
                    "count": 2,
                    "presentResources": [
                        "internalGps",
                        "gpsSatellites",
                        "bluetooth",
                        "usb",
                        "userInterface"
                    ],
                    "requiredProperties": []
                }
            ]
        }

        ig.configuration.androidEmulator.displayEmulatorGui = True
        ig.configuration.validation.minimumTestDurationMS = -1

        ig.start_olympus()

        scc = ScenarioConductorConfiguration.from_dict(scc_d)
        # scc = ScenarioConductorConfiguration.from_dict(json.load(open(args.sc_configuration_file, 'r')))
        sc = ScenarioConductor(scc, 'baseline')
        sc.execute()

    else:
        args.func(args)


if __name__ == '__main__':
    main()
