from enum import Enum


# noinspection PyPep8Naming
from typing import FrozenSet


class DeploymentModelProperty(Enum):
    def __init__(self, identifier: str):
        self.identifier: str = identifier

    trustedLocations = (
        "trustedLocations")

    @classmethod
    def all_identifier(cls) -> FrozenSet[str]:
        return frozenset([k.identifier for k in list(cls)])