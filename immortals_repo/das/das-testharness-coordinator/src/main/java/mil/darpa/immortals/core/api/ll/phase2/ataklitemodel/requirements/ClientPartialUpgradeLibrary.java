package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.UpgradableLibraryInterface;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP3
@Description("A client library upgrade that will cause a partial upgrade")
public enum ClientPartialUpgradeLibrary implements UpgradableLibraryInterface {
    @P2CP3
    Dropbox_3_0_6("Version of dropbox containing a resolution for a security flaw",
            "com.dropbox.core:dropbox-core-sdk:3.0.3",
            "com.dropbox.core:dropbox-core-sdk:3.0.6");

    public final String description;
    @Unstable
    public final String oldDependencyCoordinates;
    @Unstable
    public final String newDependencyCoordinates;

    ClientPartialUpgradeLibrary(String description, String oldDependencyCoordinates, String newDependencyCoordinates) {
        this.description = description;
        this.oldDependencyCoordinates = oldDependencyCoordinates;
        this.newDependencyCoordinates = newDependencyCoordinates;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getOldDependencyCoordinates() {
        return oldDependencyCoordinates;
    }

    @Override
    public String getNewDependencyCoordinates() {
        return newDependencyCoordinates;
    }
}
