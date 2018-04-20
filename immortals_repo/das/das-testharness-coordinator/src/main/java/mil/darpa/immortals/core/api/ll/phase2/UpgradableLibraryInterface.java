package mil.darpa.immortals.core.api.ll.phase2;

/**
 * Created by awellman@bbn.com on 4/2/18.
 */
public interface UpgradableLibraryInterface {
    
    String getDescription();
    String getOldDependencyCoordinates();
    String getNewDependencyCoordinates();
}
