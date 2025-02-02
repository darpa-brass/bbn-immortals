package mil.darpa.immortals.config;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by awellman@bbn.com on 11/1/17.
 */
public class DasServiceConfiguration implements RestfulAppConfigInterface {
    private boolean userManaged = false;
    private long startupTimeMS = 60000;
    private String identifier = "dasservice";
    private int port = 8080;
    private String protocol = "http";
    private String url = "127.0.0.1";
    private String exePath = GlobalsConfig.staticImmortalsRoot.resolve("das/das-service/das.jar").toAbsolutePath().toString();
    private String workingDirectory = GlobalsConfig.mkworkingdir("_" + identifier);
    private String workingDirectoryTemplateFolder = null;
    private String[] interpreterParameters = new String[0];
    private String[] parameters = new String[0];
    private HashMap<String, String> environmentVariables = new HashMap<>();
    String readyStdoutLineRegexPattern = ".*(?<=The DAS service located at).*(?<=is running.)$";
    private boolean shutdownEverythingOnTermination = false;
    
    private String resourceDslPath = GlobalsConfig.staticImmortalsRoot.resolve("dsl/resource-dsl").toAbsolutePath().toString();

    DasServiceConfiguration() {
    }

    @Override
    public boolean isUserManaged() {
        return userManaged;
    }

    @Override
    public long getStartupTimeMS() {
        return startupTimeMS;
    }

    @Override
    public String getIdentifier() {
        return identifier;
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
        return GlobalsConfig.toFullUrl(this);
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

    @Override
    public Path getWorkingDirectoryTemplateFolder() {
        if (workingDirectoryTemplateFolder == null) return null;
        return Paths.get(workingDirectoryTemplateFolder);
    }

    @Override
    public boolean isShutdownEverythingOnTermination() {
        return shutdownEverythingOnTermination;
    }
    
    public Path getResourceDslPath() {
        return Paths.get(resourceDslPath);
    }
}
