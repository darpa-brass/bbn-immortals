package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP3
@Description("A client upgrade library that will cause a mutation")
public enum ClientUpgradeLibrary {
    @P2CP3
    ToBeDetermined_X_X("Libraries to be determined");

    public final String description;

    ClientUpgradeLibrary(String description) {
        this.description = description;
    }
}
