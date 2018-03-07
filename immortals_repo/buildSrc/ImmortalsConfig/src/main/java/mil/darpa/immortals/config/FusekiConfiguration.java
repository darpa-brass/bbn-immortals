package mil.darpa.immortals.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by awellman@bbn.com on 12/19/17.
 */
public class FusekiConfiguration implements RestfulAppConfigInterface {
    private boolean userManaged = false;
    private long startupTimeMS = 20000;
    private String identifier = "fuseki";
    private int port = 3030;
    private String protocol = "http";
    private String url = "127.0.0.1";
    private String exePath = System.getenv("FUSEKI_HOME") == null ? "" : Paths.get(System.getenv("FUSEKI_HOME")).resolve("fuseki-server.jar").toAbsolutePath().toString();
    private String workingDirectory = GlobalsConfig.mkworkingdir("_" + identifier);
    private String[] interpreterParameters = new String[0];
    private String[] parameters = {
            "--update", "--mem", "--port=" + Integer.toString(port), "/ds"
    };
    private HashMap<String, String> environmentVariables = new HashMap<String, String>(Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<String, String>("FUSEKI_HOME", System.getenv("FUSEKI_HOME") == null ? "" : System.getenv("FUSEKI_HOME")),
            new AbstractMap.SimpleEntry<String, String>("FUSEKI_BASE", GlobalsConfig.mkworkingdir("_" + identifier + "/fuseki_base")),
            new AbstractMap.SimpleEntry<String, String>("FUSEKI_RUN", GlobalsConfig.mkworkingdir("_" + identifier + "/fuseki_run"))
    ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()))));
    private String readyStdoutLineRegexPattern = ".*(?<=Started).*(?<= on port ).*";

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
