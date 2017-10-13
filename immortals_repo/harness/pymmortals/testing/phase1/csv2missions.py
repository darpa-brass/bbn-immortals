#!/usr/bin/env python3.5

import csv
import json
import time
from typing import Set

from pymmortals.datatypes.intermediary.deploymentmodelproperty import \
    DeploymentModelProperty
from pymmortals.datatypes.intermediary.deploymentmodelresource import \
    DeploymentModelResource
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.status import Status
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase1.testadapterstate import TestAdapterState
from pymmortals.generated.mil.darpa.immortals.core.api.validation.validators import Validators
from pymmortals.datatypes.deployment_model import LLP1Input, MartiServer, ATAKLiteClient
from pymmortals.datatypes.validation import mutually_exclusive_validator_sets

parse_list = [
    {
        'y': DeploymentModelResource.internalGps.identifier,
        'n': None
    },
    {
        'y': DeploymentModelResource.gpsSatellites.identifier,
        'n': None
    },
    {
        'y': DeploymentModelResource.bluetooth.identifier,
        'n': None
    },
    {
        'y': DeploymentModelResource.usb.identifier,
        'n': None
    },
    {
        'y': DeploymentModelResource.userInterface.identifier,
        'n': None
    },
    {
        'y': DeploymentModelProperty.trustedLocations.identifier,
        'n': None
    },
    {
    },
    {
        'Yes': Status.SUCCESS.label,
        'No': Status.FAILURE.label
    },
    {
        'Yes': Status.SUCCESS.label,
        'No': Status.FAILURE.label
    }
]

dfu_validator_equivalence_dict = {
    'A-GPS': Validators.CLIENT_LOCATION_SOURCE_ANDROIDGPS,
    'BT-GPS': Validators.CLIENT_LOCATION_SOURCE_BLUETOOTH,
    'USB-GPS': Validators.CLIENT_LOCATION_SOURCE_USB,
    'SAASM-GPS': Validators.CLIENT_LOCATION_SOURCE_TRUSTED,
    'Dreck': Validators.CLIENT_LOCATION_SOURCE_MANUAL
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
    def __init__(self, missionConfiguration: MissionConfiguration, testAdapterState: TestAdapterState):
        self.missionConfiguration = missionConfiguration
        self.testAdapterState = testAdapterState


# noinspection PyPep8Naming
class MissionConfiguration:
    def __init__(self, csv_row, sessionIdentifier: str = None):
        if sessionIdentifier is None:
            sessionIdentifier = "S" + str(int(time.time() * 1000))[:12]

        self.sessionIdentifier: str = sessionIdentifier

        self.scenarioConductorConfiguration: LLP1Input = LLP1Input(
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

        self.expectedBaselineValidationResult: Status = Status(parse_list[8][csv_row[8]])

        if self.expectedBaselineValidationResult == Status.SUCCESS:
            self.expectedAugmentationResult: Status = Status.NOT_APPLICABLE
            self.expectedAugmentedValidationResult: Status = Status.NOT_APPLICABLE

        else:
            self.expectedAugmentationResult: Status = parse_list[7][csv_row[7]]
            if self.expectedAugmentationResult == Status.SUCCESS:
                self.expectedAugmentedValidationResult: Status = Status.SUCCESS
            else:
                self.expectedAugmentedValidationResult: Status = Status.NOT_APPLICABLE

        valid_providers = [v.strip() for v in csv_row[9].split(',')]
        if '' in valid_providers:
            valid_providers.remove('')

        self.expectedFailures: Set[Validators] = set(mutually_exclusive_validator_sets['client-location-source'])

        print('vp: ' + str(valid_providers))
        print('ef: ' + str(self.expectedFailures))

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
        next(reader)

        for row in reader:
            mission_configurations.append(MissionConfiguration([v.strip() for v in row]))

    return mission_configurations


def main():
    mission_configurations = read_mission_configurations('cp1.csv')

    with open('cp1.json', 'w') as f:
        json.dump(obj=[c.to_dict() for c in mission_configurations], fp=f)


if __name__ == '__main__':
    main()
