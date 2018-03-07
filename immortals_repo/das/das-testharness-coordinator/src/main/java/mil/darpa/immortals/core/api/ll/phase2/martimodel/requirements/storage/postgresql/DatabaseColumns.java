package mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP1;
import mil.darpa.immortals.core.api.annotations.Unstable;

/**
 * Created by awellman@bbn.com on 8/7/17.
 */
@Description("The possible columns the database may be constructed from. All columns must be used to construct a new schema!")
@P2CP1
public enum DatabaseColumns {
    CotEvent_SourceId("The foreign key for the source the event is associated with", "source_id", "varchar(16) not null", "integer not null"),
    CotEvent_CotType("The CoT event type", "cot_type", "varchar(16) not null", "character varying not null"),
    CotEvent_How("The standardized source type of the message", "how", "varchar(16) not null", "character varying not null"),
    CotEvent_Detail("The detail field of the CoT event", "detail", "varchar(400) not null", "text not null"),
    CotEvent_ServerTime("The timestamp for the event", "servertime", "varchar(16) not null", "bigint not null DEFAULT cast (to_char(chunk_time(current_timestamp, '5 minutes'), 'YYYYMMDDHH24MI') as bigint)"),

    Position_PointHae("Altitude", "point_hae", "varchar(16) not null", "integer not null"),
    Position_PointCE("Circular Error", "point_ce", "varchar(16) not null", "integer not null"),
    Position_PointLE("Linear Error", "point_le", "varchar(16) not null", "integer not null"),
    Position_TileX("The X tile the position is within", "tileX", "varchar(16) not null", "integer not null"),
    Position_TileY("The Y tile the position is within", "tileY", "varchar(16) not null", "integer not null"),
    Position_Longitude("The longitude of the position", "longitude", "varchar(24) not null", "double precision not null"),
    Position_Latitude("The latitude of the position", "latitude", "varchar(24) not null", "double precision not null");

    public final String description;

    @Unstable
    public final String columnName;
    @Unstable
    public final String castorDefinition;
    @Unstable
    public final String takDefinition;

    DatabaseColumns(String description, String columnName, String castorDefinition, String takDefinition) {
        this.description = description;
        this.columnName = columnName;
        this.castorDefinition = castorDefinition;
        this.takDefinition = takDefinition;
    }

    public String getCastorFieldDefinition() {
        return this.columnName + " " + this.castorDefinition;
    }

    public String getTakFieldDefinition() {
        return this.columnName + " " + this.takDefinition;
    }

}
