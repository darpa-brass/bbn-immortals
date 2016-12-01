#!/usr/bin/env python2.7

import argparse
from threading import Semaphore

import immortalsglobals
from data.scenarioconfiguration import ATAKLiteClient, MartiServer, ScenarioConfiguration
from data.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from packages import commentjson as json
from scenariorunner import ScenarioRunner

parser = argparse.ArgumentParser(description='IMMORTALS Scenario Conductor')
parser.add_argument('--sessionidentifier', type=str)
parser.add_argument('--clientcount', type=int)
parser.add_argument('--serverbandwidth', type=int)
parser.add_argument('--clientimagesendfrequency', type=int)
parser.add_argument('--clientmsgsendfrequency', type=int)
parser.add_argument('--android-bluetooth-resource', action='store_true')
parser.add_argument('--android-usb-resource', action='store_true')
parser.add_argument('--android-internal-gps-resource', action='store_true')
parser.add_argument('--android-ui-resource', action='store_true')
parser.add_argument('--gps-satellite-resource', action='store_true')
parser.add_argument('--mission-trusted-comms', action='store_true')
parser.add_argument('--file', type=str)
parser.add_argument('--configuration-string', type=str)
#
_sem = Semaphore()


class ScenarioConductor:
    """
    :type src: ScenarioRunnerConfiguration
    :type sc: ScenarioConfiguration
    :type sr: ScenarioRunner
    """

    def __init__(self,
                 scenario_configuration
                 ):
        self.sc = scenario_configuration

        self.src = ScenarioRunnerConfiguration.from_scenario_configuration(self.sc, 'validation')

        if 'trustedLocations' in self.sc.clients[0].required_properties:
            self.src.scenario.validator_identifiers.append('client-location-trusted')

        if self.sc.clients[0].image_broadcast_interval_ms > 0:
            self.src.scenario.validator_identifiers.append('client-image-produce')
            self.src.scenario.validator_identifiers.append('client-image-share')

        self.sr = None

    def execute(self):
        result = None

        with _sem:
            self.sr = ScenarioRunner(self.src)
            result = self.sr.execute_scenario()

            print "DONE"

        return result


def _execute(conductor):
    print conductor.execute()


def main():
    immortalsglobals.main_thread_cleanup_hookup()

    args = parser.parse_args()

    if args.file is not None:
        with open(args.file, 'r') as f:
            sc_j = json.load(f)
            sc = ScenarioConfiguration.from_dict(sc_j)

        conductor_instance = ScenarioConductor(sc)
        _execute(conductor_instance)

    elif args.configuration_string is not None:
        sc_j = json.load(args.configuration_string)
        sc = ScenarioConfiguration.from_dict(sc_j)

        conductor_instance = ScenarioConductor(sc)
        _execute(conductor_instance)

    else:
        if args.sessionidentifier is None:
            print "--sessionidentifier is a required parameter if the parameters are being obtained from command line arguments!"

        elif args.clientcount is None:
            print "--clientcount is a required parameter if the parameters are being obtained from command line arguments!"

        elif args.serverbandwidth is None:
            print "--serverbandwidth is a required parameter if the parameters are being obtained from command line arguments!"

        elif args.clientimagesendfrequency is None:
            print "--clientimagesendfrequency is a required parameter if the parameters are being obtained from command line arguments!"

        elif args.clientmsgsendfrequency is None:
            print "--clientmsgsendfrequency is a required parameter if the parameters are being obtained from command line arguments!"

        else:

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

            scenario_configuration = ScenarioConfiguration(
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

            conductor_instance = ScenarioConductor(scenario_configuration)
            _execute(conductor_instance)


if __name__ == '__main__':
    main()
