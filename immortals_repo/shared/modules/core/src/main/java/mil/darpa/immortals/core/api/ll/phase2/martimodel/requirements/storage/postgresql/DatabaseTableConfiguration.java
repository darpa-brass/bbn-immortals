package mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP1;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 8/7/17.
 */
@Description("The configuration for a table in a schema")
@P2CP1
public class DatabaseTableConfiguration {
//    @Description("The name of the new table column")
//    public final String tableName;

    @P2CP1
    @Description("The columns the schema table consists of")
    public LinkedList<DatabaseColumns> columns;

    public DatabaseTableConfiguration() {
    }

    public DatabaseTableConfiguration(LinkedList<DatabaseColumns> columns) {
//        this.tableName = tableName;
        this.columns = columns;
    }
}
