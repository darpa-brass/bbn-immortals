package mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.P2CP3;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.UpgradableLibraryInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by awellman@bbn.com on 9/15/17.
 */
@P2CP3
@Description("Possible Android platforms to deploy on")
public enum AndroidPlatformVersion implements UpgradableLibraryInterface {
    @P2CP3
    Android23("Newer Android API version 23 which requires runtime permission requests", "com.google:android-platform:21", "com.google:android-platform:23", "NOT_APPLICABLE", null);

    public final String description;
    @Unstable
    public final String oldDependencyCoordinates;
    @Unstable
    public final String newDependencyCoordinates;
    @Unstable
    public final String repositoryUrl;
    @Unstable
    public final String[] vulnerabilityIdentifiers;

    AndroidPlatformVersion(String description, String oldDependencyCoordinates, String newDependencyCoordinates, String repositoryUrl, String[] vulnerabilityIdentifiers) {
        this.description = description;
        this.oldDependencyCoordinates = oldDependencyCoordinates;
        this.newDependencyCoordinates = newDependencyCoordinates;
        this.repositoryUrl = repositoryUrl;
        this.vulnerabilityIdentifiers = vulnerabilityIdentifiers;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return description;
    }

    @Nonnull
    @Override
    public String getOldDependencyCoordinates() {
        return oldDependencyCoordinates;
    }

    @Nonnull
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
