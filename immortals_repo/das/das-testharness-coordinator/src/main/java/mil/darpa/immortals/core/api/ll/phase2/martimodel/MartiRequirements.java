package mil.darpa.immortals.core.api.ll.phase2.martimodel;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP1;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.ServerPartialUpgradeLibrary;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.ServerUpgradeLibrary;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.DatabasePerturbation;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP1
@P2CP3
@Description("A requirement specification for a Marti server")
public class MartiRequirements {

    @P2CP3
    @Description("A library upgrade that will trigger a partial library upgrade")
    public ServerPartialUpgradeLibrary partialLibraryUpgrade;

    @P2CP3
    @Description("A library upgrade that will trigger a mutation")
    public ServerUpgradeLibrary libraryUpgrade;

    @P2CP1
    @Description("A Database schema perturbation")
    public DatabasePerturbation postgresqlPerturbation;

    public MartiRequirements() {
    }

    public MartiRequirements(ServerPartialUpgradeLibrary partialLibraryUpgrade,
                             ServerUpgradeLibrary libraryUpgrade,
                             DatabasePerturbation postgresqlPerturbation) {
        this.partialLibraryUpgrade = partialLibraryUpgrade;
        this.libraryUpgrade = libraryUpgrade;
        this.postgresqlPerturbation = postgresqlPerturbation;
    }
}
