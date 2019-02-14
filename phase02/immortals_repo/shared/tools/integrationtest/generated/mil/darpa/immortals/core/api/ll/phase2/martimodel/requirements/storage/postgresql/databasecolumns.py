from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class DatabaseColumns(Enum):
    def __init__(self, key_idx_value: str, castorDefinition: str, columnName: str, description: str, takDefinition: str):
        self._key_idx_value = key_idx_value
        self.castorDefinition = castorDefinition  # type: str
        self.columnName = columnName  # type: str
        self.description = description  # type: str
        self.takDefinition = takDefinition  # type: str

    CotEvent_SourceId = ("CotEvent_SourceId",
        "varchar(16) not null",
        "source_id",
        "The foreign key for the source the event is associated with",
        "integer not null")

    CotEvent_CotType = ("CotEvent_CotType",
        "varchar(16) not null",
        "cot_type",
        "The CoT event type",
        "character varying not null")

    CotEvent_How = ("CotEvent_How",
        "varchar(16) not null",
        "how",
        "The standardized source type of the message",
        "character varying not null")

    CotEvent_Detail = ("CotEvent_Detail",
        "varchar(400) not null",
        "detail",
        "The detail field of the CoT event",
        "text not null")

    CotEvent_ServerTime = ("CotEvent_ServerTime",
        "varchar(16) not null",
        "servertime",
        "The timestamp for the event",
        "bigint not null")

    Position_PointHae = ("Position_PointHae",
        "varchar(16) not null",
        "point_hae",
        "Altitude",
        "integer not null")

    Position_PointCE = ("Position_PointCE",
        "varchar(16) not null",
        "point_ce",
        "Circular Error",
        "integer not null")

    Position_PointLE = ("Position_PointLE",
        "varchar(16) not null",
        "point_le",
        "Linear Error",
        "integer not null")

    Position_TileX = ("Position_TileX",
        "varchar(16) not null",
        "tileX",
        "The X tile the position is within",
        "integer not null")

    Position_TileY = ("Position_TileY",
        "varchar(16) not null",
        "tileY",
        "The Y tile the position is within",
        "integer not null")

    Position_Longitude = ("Position_Longitude",
        "varchar(24) not null",
        "longitude",
        "The longitude of the position",
        "double precision not null")

    Position_Latitude = ("Position_Latitude",
        "varchar(24) not null",
        "latitude",
        "The latitude of the position",
        "double precision not null")

    @classmethod
    def all_castorDefinition(cls) -> FrozenSet[str]:
        return frozenset([k.castorDefinition for k in list(cls)])

    @classmethod
    def all_columnName(cls) -> FrozenSet[str]:
        return frozenset([k.columnName for k in list(cls)])

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])

    @classmethod
    def all_takDefinition(cls) -> FrozenSet[str]:
        return frozenset([k.takDefinition for k in list(cls)])
