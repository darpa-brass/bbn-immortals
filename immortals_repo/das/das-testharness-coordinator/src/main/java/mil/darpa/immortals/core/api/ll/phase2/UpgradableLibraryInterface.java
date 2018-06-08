package mil.darpa.immortals.core.api.ll.phase2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by awellman@bbn.com on 4/2/18.
 */
public interface UpgradableLibraryInterface {
    
    @Nonnull
    String getDescription();
    
    @Nonnull
    String getOldDependencyCoordinates();
    
    @Nonnull
    String getNewDependencyCoordinates();

    @Nonnull
    String getRepositoryUrl();
    
    @Nullable
    String[] getVulnerabilityIdentifiers();
}
