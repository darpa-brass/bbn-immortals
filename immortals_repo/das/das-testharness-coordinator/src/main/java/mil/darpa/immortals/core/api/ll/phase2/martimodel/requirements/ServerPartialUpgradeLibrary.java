package mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.UpgradableLibraryInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by awellman@bbn.com on 9/14/17.
 */
@Description("A server library upgrade that will cause a partial upgrade")
@P2CP3
public enum ServerPartialUpgradeLibrary implements UpgradableLibraryInterface {
    ToBeDetermined_X_X("Libraries to be determined", "dummy:old:version", "dummy:new:version", "http://central.maven.org/maven2/", new String[]{"FakeVulnerability"});

    public final String description;

    @Unstable
    public final String oldDependencyCoordinates;
    @Unstable
    public final String newDependencyCoordinates;

    @Unstable
    public final String[] vulnerabilityIdentifiers;

    @Unstable
    public final String repositoryUrl;

    ServerPartialUpgradeLibrary(String description, String oldDependencyCoordinates, String newDependencyCoordinates, String repositoryUrl, String[] vulnerabilityIdentifiers) {
        this.description = description;
        this.oldDependencyCoordinates = oldDependencyCoordinates;
        this.newDependencyCoordinates = newDependencyCoordinates;
        this.repositoryUrl = repositoryUrl;
        this.vulnerabilityIdentifiers = vulnerabilityIdentifiers;
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
        return vulnerabilityIdentifiers;
    }
}
