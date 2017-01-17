#!/usr/bin/env python2.7

import argparse
from threading import Semaphore

from . import immortalsglobals as ig
from .data.base.scenarioapiconfiguration import ATAKLiteClient, MartiServer, ScenarioConductorConfiguration
from .data.base.tools import dictify
from .data.base.validationresults import ValidationResults
from .data.scenariorunnerconfiguration import ScenarioRunnerConfiguration
from .packages import commentjson as json
from .scenariorunner import ScenarioRunner

parser = argparse.ArgumentParser(description='IMMORTALS Scenario Conductor')


def add_parser_arguments(psr):
    psr.add_argument('--sessionidentifier', type=str)
    psr.add_argument('--clientcount', type=int)
    psr.add_argument('--serverbandwidth', type=int)
    psr.add_argument('--clientimagesendfrequency', type=int)
    psr.add_argument('--clientmsgsendfrequency', type=int)
    psr.add_argument('--android-bluetooth-resource', action='store_true')
    psr.add_argument('--android-usb-resource', action='store_true')
    psr.add_argument('--android-internal-gps-resource', action='store_true')
    psr.add_argument('--android-ui-resource', action='store_true')
    psr.add_argument('--gps-satellite-resource', action='store_true')
    psr.add_argument('--mission-trusted-comms', action='store_true')

    psr.add_argument('--configuration-file', type=str)
    psr.add_argument('--configuration-string', type=str)
    psr.add_argument('--scenario-template-tag', type=str)


add_parser_arguments(parser)

_sem = Semaphore()


class ScenarioConductor:
    """
    :type src: ScenarioRunnerConfiguration
    :type sc: ScenarioConductorConfiguration
    :type sr: ScenarioRunner
    """

    def __init__(self,
                 scenario_configuration,
                 scenario_template_tag,
                 swallow_and_shutdown_on_exception=True
                 ):
        self.sc = scenario_configuration

        self.src = ScenarioRunnerConfiguration.from_scenario_api_configuration(self.sc, scenario_template_tag,
                                                                               swallow_and_shutdown_on_exception)

        ig.logger().write_artifact_to_file(
            str_to_write=json.dumps(dictify(self.src)),
            target_subpath=self.src.sessionIdentifier + '-scenariorunnerconfiguration.json',
            clobber_existing=True)

        self.sr = None

    def execute(self):
        """
        :rtype: ValidationResults
        """
        result = None

        with _sem:
            self.sr = ScenarioRunner(self.src)
            result = self.sr.execute_scenario()

            print "DONE"

        return result


def _execute(conductor):
    print conductor.execute()


def main(args=None):
    ig.main_thread_cleanup_hookup()

    if args is None:
        args = parser.parse_args()

    if args.configuration_file is not None:
        if args.scenario_template_tag is None:
            print '--scenario-template-tag is required if loading configuration from file!'

        else:
            with open(args.configuration_file, 'r') as f:
                sc_j = json.load(f)
                sc = ScenarioConductorConfiguration.from_dict(sc_j)

            conductor_instance = ScenarioConductor(sc, args.scenario_template_tag)
            _execute(conductor_instance)

    elif args.configuration_string is not None:
        if args.scenario_template_tag is None:
            print '--scenario-template-tag is required if loading configuration from file!'

        else:
            sc_j = json.load(args.configuration_string)
            sc = ScenarioConductorConfiguration.from_dict(sc_j)

            conductor_instance = ScenarioConductor(sc, args.scenario_template_tag)
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

            scenario_configuration = ScenarioConductorConfiguration(
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
