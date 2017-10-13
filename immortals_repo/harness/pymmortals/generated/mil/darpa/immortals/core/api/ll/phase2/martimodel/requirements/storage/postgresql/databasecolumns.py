from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class DatabaseColumns(Enum):
    def __init__(self, description: str):
        self.description: str = description

    CotEvent_SourceId = (
        "The foreign key for the source the event is associated with")

    CotEvent_CotType = (
        "The CoT event type")

    CotEvent_How = (
        "The standardized source type of the message")

    CotEvent_Detail = (
        "The detail field of the CoT event")

    CotEvent_ServerTime = (
        "The timestamp for the event")

    Position_PointHae = (
        "Altitude")

    Position_PointCE = (
        "Circular Error")

    Position_PointLE = (
        "Linear Error")

    Position_TileX = (
        "The X tile the position is within")

    Position_TileY = (
        "The Y tile the position is within")

    Position_Longitude = (
        "The longitude of the position")

    Position_Latitude = (
        "The latitude of the position")

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])