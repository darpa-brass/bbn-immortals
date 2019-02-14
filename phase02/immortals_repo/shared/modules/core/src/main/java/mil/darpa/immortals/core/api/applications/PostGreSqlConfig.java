package mil.darpa.immortals.core.api.applications;

/**
 * Created by awellman@bbn.com on 9/18/17.
 */
public class PostGreSqlConfig {
    public final boolean enabled;
    public final String serverName;
    public final int serverPort;
    public final String serverUsername;
    public final String serverPassword;

    public PostGreSqlConfig(boolean enabled, String serverName, int serverPort, String serverUsername, String serverPassword) {
        this.enabled = enabled;
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.serverUsername = serverUsername;
        this.serverPassword = serverPassword;
    }
}
