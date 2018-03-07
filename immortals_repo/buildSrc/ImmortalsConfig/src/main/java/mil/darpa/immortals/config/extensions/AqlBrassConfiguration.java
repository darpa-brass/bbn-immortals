package mil.darpa.immortals.config.extensions;

import mil.darpa.immortals.config.GlobalsConfig;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.RestfulAppConfigInterface;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by awellman@bbn.com on 2/20/18.
 */
public class AqlBrassConfiguration implements MavenArtifactInterface, RestfulAppConfigInterface {
    private boolean userManaged = false;
    private long startupTimeMS = 20000;
    private String identifier = "aqlbrass";
    
    private final String mavenRepositoryUrl = "https://github.com/babeloff/mvn-repo/raw/master/releases";
    private final String mavenGroupId = "babeloff";
    private final String mavenArtifactId = "brass-aql-server";
    private final String mavenVersion = "2018.03.16";

    private int port = 9090;
    private String protocol = "http";
    private String url = "127.0.0.1";
    private String workingDirectory = GlobalsConfig.mkworkingdir("_" + identifier);
    private String exePath = GlobalsConfig.getExtensionsDownloadDir().resolve(mavenArtifactId + ".jar").toString();
    private String[] interpreterParameters  = new String[0];
    private String[] parameters = {
            "--hostname",
            url,
            "--port",
            Integer.toString(port)
    };
    private HashMap<String, String> environmentVariables = new HashMap<>();

    public AqlBrassConfiguration() {

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
    public String getExePath() {
        return exePath;
    }

    public Path getExecutionWorkingDirectory(@Nonnull String adaptationIdentifier) {
        return ImmortalsConfig.getInstance().globals.getAdaptationComponentWorkingDirectory(adaptationIdentifier, identifier);
    }

    @Override
    public String getWorkingDirectory() {
        return Paths.get(GlobalsConfig.mkworkingdir("_" + identifier)).toString();
    }

    @Override
    public String[] getInterpreterParameters() {
        return Arrays.copyOf(interpreterParameters, interpreterParameters.length);
    }

    @Override
    public String[] getParameters() {
        return Arrays.copyOf(parameters, parameters.length);
    }

    @Override
    public HashMap<String, String> getEnvironmentVariables() {
        return new HashMap<>(environmentVariables);
    }

    @Override
    public String getMavenRepositoryUrl() {
        return mavenRepositoryUrl;
    }

    @Override
    public String getMavenGroupId() {
        return mavenGroupId;
    }

    @Override
    public String getMavenArtifactId() {
        return mavenArtifactId;
    }

    @Override
    public String getMavenVersion() {
        return mavenVersion;
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
        return null;
    }
    
    @Override
    public String getMavenFullDependencyCoordinate() {
        return MavenArtifactInterface.toDependencyCoordinate(this);
    }
}
