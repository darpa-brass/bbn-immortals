package mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP1;

/**
 * Created by awellman@bbn.com on 8/7/17.
 */
@Description("The possible columns the database may be constructed from. All columns must be used to construct a new schema!")
@P2CP1
public enum DatabaseColumns {
    CotEvent_SourceId("The foreign key for the source the event is associated with"),
    CotEvent_CotType("The CoT event type"),
    CotEvent_How("The standardized source type of the message"),
    CotEvent_Detail("The detail field of the CoT event"),
    CotEvent_ServerTime("The timestamp for the event"),

    Position_PointHae("Altitude"),
    Position_PointCE("Circular Error"),
    Position_PointLE("Linear Error"),
    Position_TileX("The X tile the position is within"),
    Position_TileY("The Y tile the position is within"),
    Position_Longitude("The longitude of the position"),
    Position_Latitude("The latitude of the position");

    public final String description;

    DatabaseColumns(String description) {
        this.description = description;
    }
}
