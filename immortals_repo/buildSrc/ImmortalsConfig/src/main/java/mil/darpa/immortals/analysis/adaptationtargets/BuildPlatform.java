package mil.darpa.immortals.analysis.adaptationtargets;

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
