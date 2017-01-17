#!/usr/bin/env python2.7

import csv
import json
import time

from scenarioconductor import *


# from data.base.scenarioapiconfiguration import ATAKLiteClient, MartiServer, ScenarioConductorConfiguration
# from scenarioconductor.ll_api.data import Status, TestAdapterState
# from scenarioconductor.validation import PROPERTIES, RESOURCES, VALIDATORS, mutually_exclusive_validator_sets

parse_list = [
    {
        'y': RESOURCES.internal_gps,
        'n': None
    },
    {
        'y': RESOURCES.gps_satellites,
        'n': None
    },
    {
        'y': RESOURCES.bluetooth,
        'n': None
    },
    {
        'y': RESOURCES.usb,
        'n': None
    },
    {
        'y': RESOURCES.user_interface,
        'n': None
    },
    {
        'y': PROPERTIES.trustedLocations,
        'n': None
    },
    {
    },
    {
        'Yes': Status.SUCCESS,
        'No': Status.FAILURE
    },
    {
        'Yes': Status.SUCCESS,
        'No': Status.FAILURE
    }
]

dfu_validator_equivalence_dict = {
    'A-GPS': VALIDATORS.client_location_source_androidgps,
    'BT-GPS': VALIDATORS.client_location_source_bluetooth,
    'USB-GPS': VALIDATORS.client_location_source_usb,
    'SAASM-GPS': VALIDATORS.client_location_source_trusted,
    'Dreck': VALIDATORS.client_location_source_manual
}


def _parse_range(csv_row, start_idx, end_idx):
    s = set()

    for i in range(start_idx, end_idx):
        s.add(parse_list[i][csv_row[i]])

    if None in s:
        s.remove(None)

    return s


# noinspection PyPep8Naming
class MissionValidationResult:
    """
    :type missionConfiguration: MissionConfiguration
    :type testAdapterState: TestAdapterState
    """

    def __init__(self, missionConfiguration, testAdapterState):
        self.missionConfiguration = missionConfiguration
        self.testAdapterState = testAdapterState


# noinspection PyPep8Naming
class MissionConfiguration:
    """
    :type sessionIdentifier: str
    :type scenarioConductorConfiguration: ScenarioConductorConfiguration
    :type expectedBaselineValidationResult: str
    :type expectedAugmentationResult: str
    :type expectedAugmentedValidationResult: str
    :type expectedFailures: set[str]
    """

    def __init__(self, csv_row, sessionIdentifier=None):
        if sessionIdentifier is None:
            sessionIdentifier = "S" + str(int(time.time() * 1000))[:12]

        self.sessionIdentifier = sessionIdentifier

        self.scenarioConductorConfiguration = ScenarioConductorConfiguration(
            sessionIdentifier=sessionIdentifier,
            server=MartiServer(
                100000
            ),
            clients=[
                ATAKLiteClient(
                    imageBroadcastIntervalMS=2000,
                    latestSABroadcastIntervalMS=1000,
                    count=2,
                    presentResources=list(_parse_range(csv_row, 0, 5)),
                    requiredProperties=list(_parse_range(csv_row, 5, 6))
                )
            ]
        )

        csv_row = [v.strip() for v in csv_row]

        self.expectedBaselineValidationResult = parse_list[8][csv_row[8]]

        if self.expectedBaselineValidationResult == Status.SUCCESS:
            self.expectedAugmentationResult = Status.NOT_APPLICABLE
            self.expectedAugmentedValidationResult = Status.NOT_APPLICABLE

        else:
            self.expectedAugmentationResult = parse_list[7][csv_row[7]]
            if self.expectedAugmentationResult == Status.SUCCESS:
                self.expectedAugmentedValidationResult = Status.SUCCESS
            else:
                self.expectedAugmentedValidationResult = Status.NOT_APPLICABLE

        valid_providers = [v.strip() for v in csv_row[9].split(',')]
        if '' in valid_providers:
            valid_providers.remove('')

        self.expectedFailures = set(mutually_exclusive_validator_sets['client-location-source'])

        print 'vp: ' + str(valid_providers)
        print 'ef: ' + str(self.expectedFailures)

        for v in valid_providers:
            self.expectedFailures.remove(dfu_validator_equivalence_dict[v])

    def to_dict(self):
        return {
            'sessionIdentifier': self.sessionIdentifier,
            'scenarioConductorConfiguration': self.scenarioConductorConfiguration.to_dict(),
            'expectedBaselineValidationResult': self.expectedBaselineValidationResult,
            'expectedAugmentationResult': self.expectedAugmentationResult,
            'expectedAugmentedValidationResult': self.expectedAugmentedValidationResult,
            'expectedFailures': list(self.expectedFailures)
        }


def read_mission_configurations(configuration_csv_filepath):
    """
    :type: configuration_csv_filepath: str
    :rtype: list[MissionConfiguration]
    """

    mission_configurations = []
    with open(configuration_csv_filepath, 'r') as f:
        reader = csv.reader(f)
        reader.next()

        for row in reader:
            mission_configurations.append(MissionConfiguration([v.strip() for v in row]))

    return mission_configurations


def main():
    mission_configurations = read_mission_configurations('cp1.csv')

    with open('cp1.json', 'w') as f:
        json.dump(obj=[c.to_dict() for c in mission_configurations], fp=f)


if __name__ == '__main__':
    main()
