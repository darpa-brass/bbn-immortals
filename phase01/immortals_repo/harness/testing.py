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
    psr.add_argument('flow', metavar='FLOW', choices=['baseline', 'challenge', 'all'])
    psr.add_argument('deployment_model',
                     metavar='DEPLOYMENT_MODEL',
                     choices=['baseline', 'fail-all', 'fail-gps', 'fail-bandwidth', 'custom'])
    psr.add_argument('-f', '--scenario-file', type=str)
    psr.add_argument('-s', '--scenario-string', type=str)


def llds_help():
    return """
    FLOW Options:
    baseline        Executes the baseline validation scenario with the provided
                        deployment model
    challenge       Executes augmentation and validation against the provided
                        deployment model
    all             Executes baseline-a, baseline-b with the provided deployment
                    model, and challenge with the provided deployment model
                    sequentially

    DEPLOYMENT_MODEL Options:
    baseline        Baseline deployment model
    fail-all        Deployment model where GPS and bandwidth both fail
    fail-gps        Deployment model where GPS fails but bandwidth succeeds
    fail-bandwidth  Deployment model where bandwidth fails but GPS succeeds
    custom          Use a custom deployment model
    """


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


def tools_dmttl_main(args=None):
    from scenarioconductor.data.base.scenarioapiconfiguration import ScenarioConductorConfiguration
    from scenarioconductor.ttl_bridge import execute_ttl_generation
    import commentjson as json
    args = _parser.parse_args()
    sc_d = json.load(open(args.input_file, 'r'))
    sc_obj = ScenarioConductorConfiguration.from_dict(sc_d)
    execute_ttl_generation(sc_obj, args.output_file)


_sub_parsers = _parser.add_subparsers(help='Available Commands')

llds_parser = _sub_parsers.add_parser('llds', help='IMMoRTALS Mock LL TestHarness')

ll_dummy_server_parser = _sub_parsers.add_parser('llds', help='IMMORTALS Mock LL TH', epilog=llds_help(),
                                                 formatter_class=argparse.RawTextHelpFormatter)
llds_add_parser_arguments(ll_dummy_server_parser)
ll_dummy_server_parser.set_defaults(func=llds_main)

sc_parser = _sub_parsers.add_parser('sc', help='IMMoRTALS Scenario Conductor')
sc_parser.add_argument('-f', '--sc-configuration-file', type=str)

olympus_parser = _sub_parsers.add_parser('olympus', help='IMMoRTALS Olympus Web Interface')
olympus_parser.set_defaults(func=olympus_main)

tools_parser = _sub_parsers.add_parser('tools', help='IMMoRTALS Tools')
_tools_parser_subparsers = tools_parser.add_subparsers(help='Available Utility Commands')
dmttl_parser = _tools_parser_subparsers.add_parser('dmtottl', help="Deployment Model To TTL")
dmttl_parser.add_argument('input_file', metavar='INPUT_FILE')
dmttl_parser.add_argument('output_file', metavar='OUTPUT_FILE')
dmttl_parser.set_defaults(func=tools_dmttl_main)

network_logger = logging.getLogger


def setup_emulators_main(args=None):
    from scenarioconductor.android.emuhelper import initially_setup_emulators
    initially_setup_emulators()

emulator_parser = _sub_parsers.add_parser('setupemulators')
emulator_parser.set_defaults(func=setup_emulators_main)


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

        ig.configuration.validationEnvironment.displayAndroidEmulatorGui = True
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
