#!/usr/bin/env python2.7

import argparse
import json
import pprint
import signal
import sys

from data.scenarioconfiguration import ATAKLiteClient, MartiServer, ScenarioConfiguration
from data.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from processrouter import ProcessRouter
from scenariorunner import ScenarioRunner

proto = False

if proto:
    proto_parser = argparse.ArgumentParser()
    proto_parser.add_argument('--port', type=int, metavar='PORT')
    proto_parser.add_argument('--file', type=str, metavar='FILE')

parser = argparse.ArgumentParser(description='IMMoRTALS Scenario Orchestrator')
parser.add_argument('--sessionidentifier', required=True, type=str, metavar='UNIQUE_BASE36_IDENTIFIER')
parser.add_argument('--clientcount', required=True, type=int, metavar='CLIENT_COUNT')
parser.add_argument('--serverbandwidth', required=True, type=int, metavar='SERVER_BANDWIDTH')
parser.add_argument('--clientimagesendfrequency', required=True, type=int, metavar='CLIENT_IMAGE_SEND_FREQUENCY')
parser.add_argument('--clientmsgsendfrequency', required=True, type=int, metavar='CLIENT_LATEST_SA_SEND_FREQUENCY')
parser.add_argument('--android-bluetooth-resource', action='store_true')
parser.add_argument('--android-usb-resource', action='store_true')
parser.add_argument('--android-internal-gps-resource', action='store_true')
parser.add_argument('--android-ui-resource', action='store_true')
parser.add_argument('--gps-satellite-resource', action='store_true')
parser.add_argument('--mission-trusted-comms', action='store_true')

exit_handlers = []


def _signal_handler(signal, frame):
    # print "Exiting sc"
    sys.exit(0)
    for handler in exit_handlers:
        handler()


signal.signal(signal.SIGINT, _signal_handler)


class ScenarioConductor:
    """
    :type src: ScenarioRunnerConfiguration
    :type sc: ScenarioConfiguration
    :type pp: pprint.PrettyPrinter
    :type sr: ScenarioRunner
    """

    def __init__(self,
                 scenario_configuration  # type: ScenarioConfiguration
                 ):
        self.sc = scenario_configuration  # type:ScenarioConfiguration

        self.src = ScenarioRunnerConfiguration.from_scenario_configuration(self.sc, 'validation')

        if 'trustedLocations' in self.sc.clients[0].required_properties:
            self.src.scenario.validator_identifiers.append('client-location-trusted')

        if self.sc.clients[0].image_broadcast_interval_ms > 0:
            self.src.scenario.validator_identifiers.append('client-image-produce')
            self.src.scenario.validator_identifiers.append('client-image-share')

        # print json.dumps(self.src.clone_and_trim(), default=lambda o: o.__dict__, sort_keys=True, indent=4)

        self.sr = None

    def execute(self):
        # print "Executing scenario with the following configuration:"
        self.sr = ScenarioRunner(self.src, 'validation')
        self.sr.execute_scenario()


# def _configuration_listener(message):
#    print "received"
#    print message
#    print json.loads(message)
#    input = json.loads(message)
#    output = ScenarioConverter.produce_scenario(input)
#    conductor_instance = ScenarioConductor(json.loads(message))
#    _execute(conductor_instance)


def _execute(conductor):
    print conductor.execute()


def main():
    global exit_handlers
    exit_handlers.append(ProcessRouter.exit_handler)

    if proto:
        proto_args = proto_parser.parse_args()

        if proto_args.port is not None:
            pass
        # SocketServer.add_message_listener(_configuration_listener).start(proto_args.port)
        #        while ProcessRouter.keep_running():
        #            time.sleep(1)

        elif proto_args.file is not None:
            with open(proto_args.file, 'r') as f:
                sc_j = json.load(f)
                sc = ScenarioConfiguration.from_dict(sc_j)

            conductor_instance = ScenarioConductor(sc)
            _execute(conductor_instance)

    else:
        args = parser.parse_args()
        available_client_resources = []
        required_properties = []
        if args.android_bluetooth_resource:
            available_client_resources.append('bluetooth')

        if args.android_usb_resource:
            available_client_resources.append('usb')

        if args.android_internal_gps_resource:
            available_client_resources.append('internalGps')

        if args.android_ui_resource:
            available_client_resources.append('userInterface')

        if args.gps_satellite_resource:
            available_client_resources.append('gpsSatellites')

        if args.mission_trusted_comms:
            required_properties.append('trustedLocations')

        scencario_configuration = ScenarioConfiguration(
                args.sessionidentifier,
                MartiServer(
                        args.serverbandwidth
                ),
                [
                    ATAKLiteClient(
                            60 / int(args.clientimagesendfrequency),
                            60 / int(args.clientmsgsendfrequency),
                            args.clientcount,
                            available_client_resources,
                            required_properties
                    )
                ]
        )

        src = ScenarioRunnerConfiguration.from_scenario_configuration(scencario_configuration, 'validation')

        conductor_instance = ScenarioConductor(scencario_configuration)
        _execute(conductor_instance)


if __name__ == '__main__':
    main()
