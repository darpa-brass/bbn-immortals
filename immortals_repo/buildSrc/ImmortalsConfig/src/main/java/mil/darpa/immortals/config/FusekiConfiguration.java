package mil.darpa.immortals.config;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by awellman@bbn.com on 12/19/17.
 */
public class FusekiConfiguration implements RestfulAppConfigInterface {
    
    private static HashMap<String, String> genParameters(String identifier) {
        String fuseki_home = System.getenv("FUSEKI_HOME");
        if (fuseki_home == null) {
            fuseki_home = "";
        }
        
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("FUSEKI_HOME", fuseki_home);
        parameters.put("FUSEKI_BASE", GlobalsConfig.mkworkingdir("_" + identifier + "/fuseki_base"));
        parameters.put("FUSEKI_RUN", GlobalsConfig.mkworkingdir("_" + identifier + "/fuseki_run"));
        return parameters;
    }
    
    private boolean userManaged = false;
    private long startupTimeMS = 20000;
    private String identifier = "fuseki";
    private int port = 3030;
    private String protocol = "http";
    private String url = "127.0.0.1";
    private String exePath = System.getenv("FUSEKI_HOME") == null ? "" : Paths.get(System.getenv("FUSEKI_HOME")).resolve("fuseki-server.jar").toAbsolutePath().toString();
    private String workingDirectory = GlobalsConfig.mkworkingdir("_" + identifier);
    private String workingDirectoryTemplateFolder;
    private String[] interpreterParameters = new String[0];
    private String[] parameters = {
            "--update", "--mem", "--port=" + Integer.toString(port), "/ds"
    };
    private HashMap<String, String> environmentVariables = genParameters(identifier);
    private String readyStdoutLineRegexPattern = ".*(?<=Started).*(?<= on port ).*";
    private boolean shutdownEverythingOnTermination = false;
    
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
        return GlobalsConfig.toFullUrl(this);
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

    @Override
    public Path getWorkingDirectoryTemplateFolder() {
        if (workingDirectoryTemplateFolder == null) return null;
        return Paths.get(workingDirectoryTemplateFolder);
    }

    @Override
    public boolean isShutdownEverythingOnTermination() {
        return shutdownEverythingOnTermination;
    }
}
