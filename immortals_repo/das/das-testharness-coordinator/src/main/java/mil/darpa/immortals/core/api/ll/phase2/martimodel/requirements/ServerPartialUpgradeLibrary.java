package mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.ll.phase2.UpgradableLibraryInterface;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@Description("A server library upgrade that will cause a partial upgrade")
@P2CP3
public enum ServerPartialUpgradeLibrary implements UpgradableLibraryInterface {
    Dom4jCot_2("Newer cot processing library", "OldLib", "NewLib");

    public final String description;
    public final String oldDependencyCoordinates;
    public final String newDependencyCoordinates;

    ServerPartialUpgradeLibrary(String description, String oldDependencyCoordinates, String newDependencyCoordinates) {
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
