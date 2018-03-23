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
    CotEvent_SourceId("source_id", "The foreign key for the source the event is associated with", "varchar(16) not null", "integer not null"),
    CotEvent_CotType("cot_type", "The CoT event type", "varchar(16) not null", "character varying not null"),
    CotEvent_How("how", "The standardized source type of the message", "varchar(16) not null", "character varying not null"),
    CotEvent_Detail("detail", "The detail field of the CoT event", "varchar(400) not null", "text not null"),
    CotEvent_ServerTime("servertime", "The timestamp for the event", "varchar(16) not null", "bigint not null"),

    Position_PointHae("point_hae", "Altitude", "varchar(16) not null", "integer not null"),
    Position_PointCE("point_ce", "Circular Error", "varchar(16) not null", "integer not null"),
    Position_PointLE("point_le", "Linear Error", "varchar(16) not null", "integer not null"),
    Position_TileX("tileX", "The X tile the position is within", "varchar(16) not null", "integer not null"),
    Position_TileY("tileY", "The Y tile the position is within", "varchar(16) not null", "integer not null"),
    Position_Longitude("longitude", "The longitude of the position", "varchar(24) not null", "double precision not null"),
    Position_Latitude("latitude", "The latitude of the position", "varchar(24) not null", "double precision not null");

    @Unstable
    public final String columnName;
    @Unstable
    public final String castorDefinition;
    @Unstable
    public final String takDefinition;
    public final String description;
    
    DatabaseColumns(String columnName, String description, String castorDefinition, String takDefinition) {
    	this.columnName = columnName;
    	this.description = description;
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
