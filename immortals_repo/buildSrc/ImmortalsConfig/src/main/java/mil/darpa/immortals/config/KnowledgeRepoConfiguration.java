package mil.darpa.immortals.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by awellman@bbn.com on 12/11/17.
 */
public class KnowledgeRepoConfiguration implements RestfulAppConfigInterface {
    private boolean userManaged = false;
    private long startupTimeMS = 60000;
    private String identifier = "knowledgerepo";
    private String url = "127.0.0.1";
    private String protocol = "http";
    private int port = 9999;

    private String exePath = GlobalsConfig.staticImmortalsRoot.resolve("knowledge-repo/knowledge-repo/repository-service/target/immortals-repository-service-boot.war").toAbsolutePath().toString();
    private String workingDirectory = GlobalsConfig.mkworkingdir("_" + identifier);
    private String[] interpreterParameters = {
            "-Djava.security.egd=file:/dev/urandom",
            "-Dserver.address=" + url,
            "-Dserver.port=" + Integer.toString(port)
    };
    private String readyStdoutLineRegexPattern = "^running ImmortalsRepositoryService$";

    private String[] parameters = new String[0];
    private HashMap<String, String> environmentVariables = new HashMap<>();

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

    @Override
    public String getReadyStdoutLineRegexPattern() {
        return readyStdoutLineRegexPattern;
    }
}
