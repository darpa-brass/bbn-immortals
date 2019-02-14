package mil.darpa.immortals.core.das.adaptationmodules.partiallibraryupgrade;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 2/28/18.
 */
public class PartialLibraryUpgradeInitializationObject {
    
    private class Artifact {
        private final String jarFilepath;
        
        public Artifact(@Nonnull String jarFilepath) {
            this.jarFilepath = jarFilepath;
        }
    }
    
    private final Artifact currentlyUsedLibrary;
    
    private final Artifact targetUpgradeLibrary;
    
    private final LinkedList<Artifact> targetApplications;
    
    public PartialLibraryUpgradeInitializationObject(@Nonnull String olderLibraryFilepath, @Nonnull String newerLibraryFilepath) {
        this.currentlyUsedLibrary = new Artifact(olderLibraryFilepath);
        this.targetUpgradeLibrary = new Artifact(newerLibraryFilepath);
        this.targetApplications = new LinkedList<>(Arrays.asList(
                new Artifact("/absolute/path/to/application/using/currently/used/library")
        ));
    }
}
