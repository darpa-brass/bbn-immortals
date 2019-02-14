package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.UpgradableLibraryInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@P2CP3
@Description("A client upgrade library that will cause a mutation")
public enum ClientUpgradeLibrary implements UpgradableLibraryInterface {
    @P2CP3
    NONE("No Applicable Libraries Included", "dummy:old:version", "dummy:new:version", "http://central.maven.org/maven2/");

    public final String description;
    public final String oldDependencyCoordinates;
    public final String newDependencyCoordinates;
    @Unstable
    public final String repositoryUrl;

    ClientUpgradeLibrary(String description, String oldDependencyCoordinates, String newDependencyCoordinates, String repositoryUrl) {
        this.description = description;
        this.oldDependencyCoordinates = oldDependencyCoordinates;
        this.newDependencyCoordinates = newDependencyCoordinates;
        this.repositoryUrl = repositoryUrl;
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

    @Nonnull
    @Override
    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    @Nullable
    @Override
    public String[] getVulnerabilityIdentifiers() {
        return null;
    }
}
