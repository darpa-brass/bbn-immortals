package mil.darpa.immortals.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by awellman@bbn.com on 11/1/17.
 */
public class TestAdapterConfiguration implements RestfulAppConfigInterface {
    private boolean userManaged = false;
    private long startupTimeMS = 20000;
    private String identifier = "testadapter";
    private int port = 80;
    private String protocol = "http";
    private String url = "brass-ta";
    private String exePath = GlobalsConfig.staticImmortalsRoot.resolve("das/das-testharness-coordinator/build/libs/das-testharness-coordinator-2.0-LOCAL.jar").toAbsolutePath().toString();
    private String workingDirectoryTemplateFolder = null;
    private String workingDirectory = GlobalsConfig.mkworkingdir("_" + identifier);
    private String[] interpreterParameters = new String[0];
    private String[] parameters = new String[0];
    private HashMap<String, String> environmentVariables = new HashMap<>();

    private int websocketPort = 7878;
    private String readyStdoutLineRegexPattern = ".*(?<=Started TestAdapter at URL ').*(?<=\\.)$";

    TestAdapterConfiguration() {
    }

    @Override
    public boolean isUserManaged() {
        return userManaged;
    }

    public long getStartupTimeMS() {
        return startupTimeMS;
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
    public Path getWorkingDirectoryTemplateFolder() {
        if (workingDirectoryTemplateFolder == null) return null;
        return Paths.get(workingDirectoryTemplateFolder);
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

    public int getWebsocketPort() {
        return websocketPort;
    }

    @Override
    public String getReadyStdoutLineRegexPattern() {
        return readyStdoutLineRegexPattern;
    }
}
