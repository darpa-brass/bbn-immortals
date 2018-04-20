from enum import Enum
from typing import FrozenSet


# noinspection PyPep8Naming
class DatabaseColumns(Enum):
    def __init__(self, key_idx_value: str, castorDefinition: str, columnName: str, description: str, takDefinition: str):
        self._key_idx_value = key_idx_value
        self.castorDefinition = castorDefinition  # type: str
        self._key_idx_value = key_idx_value
        self.columnName = columnName  # type: str
        self._key_idx_value = key_idx_value
        self.description = description  # type: str
        self._key_idx_value = key_idx_value
        self.takDefinition = takDefinition  # type: str

    CotEvent_SourceId = ("CotEvent_SourceId",
        "The foreign key for the source the event is associated with",
        "source_id",
        "integer not null",
        "varchar(16) not null")

    CotEvent_CotType = ("CotEvent_CotType",
        "The CoT event type",
        "cot_type",
        "character varying not null",
        "varchar(16) not null")

    CotEvent_How = ("CotEvent_How",
        "The standardized source type of the message",
        "how",
        "character varying not null",
        "varchar(16) not null")

    CotEvent_Detail = ("CotEvent_Detail",
        "The detail field of the CoT event",
        "detail",
        "text not null",
        "varchar(400) not null")

    CotEvent_ServerTime = ("CotEvent_ServerTime",
        "The timestamp for the event",
        "servertime",
        "bigint not null",
        "varchar(16) not null")

    Position_PointHae = ("Position_PointHae",
        "Altitude",
        "point_hae",
        "integer not null",
        "varchar(16) not null")

    Position_PointCE = ("Position_PointCE",
        "Circular Error",
        "point_ce",
        "integer not null",
        "varchar(16) not null")

    Position_PointLE = ("Position_PointLE",
        "Linear Error",
        "point_le",
        "integer not null",
        "varchar(16) not null")

    Position_TileX = ("Position_TileX",
        "The X tile the position is within",
        "tileX",
        "integer not null",
        "varchar(16) not null")

    Position_TileY = ("Position_TileY",
        "The Y tile the position is within",
        "tileY",
        "integer not null",
        "varchar(16) not null")

    Position_Longitude = ("Position_Longitude",
        "The longitude of the position",
        "longitude",
        "double precision not null",
        "varchar(24) not null")

    Position_Latitude = ("Position_Latitude",
        "The latitude of the position",
        "latitude",
        "double precision not null",
        "varchar(24) not null")

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
