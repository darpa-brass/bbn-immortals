package mil.darpa.immortals.config.extensions;

import mil.darpa.immortals.config.GlobalsConfig;
import mil.darpa.immortals.config.RestfulAppConfigInterface;

import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by awellman@bbn.com on 3/15/18.
 */
public class VoltDBConfiguration implements RestfulAppConfigInterface {
    private boolean userManaged = false;
    private long startupTimeMS = 20000;
    private String identifier = "voltdb";
    private int port = 21212;
    private String protocol = "http";
    private String url = "127.0.0.1";
    private String workingDirectory = GlobalsConfig.staticImmortalsRoot.resolve("castor/voltdb7").toString();
    private String exePath = Paths.get(workingDirectory).resolve("build.xml").toString();
    private String[] interpreterParameters = { "-buildfile" };
    private String[] parameters = {"clean", "init", "start"};
    private HashMap<String, String> environmentVariables = new HashMap<>();
    private String readyStdoutLineRegexPattern = " *\\[exec\\] Server completed initialization\\.";

    @Override
    public boolean isUserManaged() {
        return userManaged;
    }

    public long getStartupTimeMS() {
        return startupTimeMS;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public URI getFullUrl() {
        return RestfulAppConfigInterface.toFullUrl(this);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getExePath() {
        return exePath;
    }

    @Override
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public String[] getInterpreterParameters() {
        return interpreterParameters;
    }

    @Override
    public String[] getParameters() {
        return parameters;
    }

    @Override
    public HashMap<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    @Override
    public String getReadyStdoutLineRegexPattern() {
        return readyStdoutLineRegexPattern;
    }
}
