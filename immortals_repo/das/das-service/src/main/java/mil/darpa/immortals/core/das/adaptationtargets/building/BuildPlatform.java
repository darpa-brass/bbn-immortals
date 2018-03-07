package mil.darpa.immortals.core.das.adaptationtargets.building;

/**
 * Created by awellman@bbn.com on 1/25/18.
 */
public enum BuildPlatform {
    GRADLE("gradle");
    
    public final String command;
    
    private BuildPlatform(String command) {
        this.command = command;
    }
}
