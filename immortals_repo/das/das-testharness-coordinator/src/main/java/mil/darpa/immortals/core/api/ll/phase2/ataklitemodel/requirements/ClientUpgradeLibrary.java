package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.ll.phase2.UpgradableLibraryInterface;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP3
@Description("A client upgrade library that will cause a mutation")
public enum ClientUpgradeLibrary implements UpgradableLibraryInterface {
    @P2CP3
    ToBeDetermined_X_X("Libraries to be determined", "dummy:old:version", "dummy:new:version");

    public final String description;
    public final String oldDependencyCoordinates;
    public final String newDependencyCoordinates;

    ClientUpgradeLibrary(String description, String oldDependencyCoordinates, String newDependencyCoordinates) {
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
