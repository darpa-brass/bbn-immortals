package mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.ll.phase2.UpgradableLibraryInterface;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@Description("A server upgrade library that will cause a mutation")
@P2CP3
public enum ServerUpgradeLibrary implements UpgradableLibraryInterface {
    ElevationApi_2(
            "Elevation API that provides security fixes and improved accuracy but requires a network connection",
            "mil.darpa.immortals.dfus:ElevationApi-1:2.0-LOCAL",
            "mil.darpa.immortals.dfus:ElevationApi-2:2.0-LOCAL");

    public final String description;
    public final String oldDependencyCoordinates;
    public final String newDependencyCoordinates;

    ServerUpgradeLibrary(String description, String oldDependencyCoordinates, String newDependencyCoordinates) {
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
