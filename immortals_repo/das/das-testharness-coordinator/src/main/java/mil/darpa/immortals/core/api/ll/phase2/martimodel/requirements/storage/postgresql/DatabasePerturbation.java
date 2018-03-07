package mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP1;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 8/7/17.
 */
@P2CP1
@Description("Database schema perturbation specification")
public class DatabasePerturbation {

    @P2CP1
    @Description("The tables the new schema should consist of")
    public LinkedList<DatabaseTableConfiguration> tables;

    public DatabasePerturbation() {
    }

    public DatabasePerturbation(List<DatabaseTableConfiguration> tables) {
        tables = new LinkedList<>(tables);
    }
}
