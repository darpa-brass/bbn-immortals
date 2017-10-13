from enum import Enum


# noinspection PyPep8Naming
from typing import FrozenSet


class DeploymentModelResource(Enum):
    def __init__(self, identifier: str):
        self.identifier: str = identifier

    bluetooth = (
        "bluetooth")

    usb = (
        "usb")

    internalGps = (
        "internalGps")

    userInterface = (
        "userInterface")

    gpsSatellites = (
        "gpsSatellites")

    @classmethod
    def all_identifier(cls) -> FrozenSet[str]:
        return frozenset([k.identifier for k in list(cls)])