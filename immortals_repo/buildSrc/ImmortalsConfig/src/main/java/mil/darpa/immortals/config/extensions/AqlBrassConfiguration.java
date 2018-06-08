package mil.darpa.immortals.config.extensions;

import mil.darpa.immortals.config.GlobalsConfig;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.RestfulAppConfigInterface;

import javax.annotation.Nonnull;
import java.net.URI;
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

    private final String mavenGroupId = "aql-brass-server";
    private final String mavenArtifactId = "aql-brass-server";
    private final String mavenArtifactExtension = "jar";

    private int port = 9090;
    private String protocol = "http";
    private String url = "127.0.0.1";
    private String workingDirectory = GlobalsConfig.mkworkingdir("_" + identifier);
    private String workingDirectoryTemplateFolder = null;
    private String exePath = GlobalsConfig.mkextensiondir("vanderbilt").resolve(mavenGroupId + "-" + mavenArtifactId + "." + mavenArtifactExtension).toString();
    private String[] interpreterParameters = new String[0];
    private String[] parameters = {
            "--hostname",
            url,
            "--port",
            Integer.toString(port)
    };
    private HashMap<String, String> environmentVariables = new HashMap<>();

    private String readyStdoutLineRegexPattern = "^STATE:\\[RUNNING\\]$";

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
    public String getMavenGroupId() {
        return mavenGroupId;
    }

    @Override
    public String getMavenArtifactId() {
        return mavenArtifactId;
    }

    @Override
    public String getMavenArtifactExtension() {
        return mavenArtifactExtension;
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

    @Override
    public Path getWorkingDirectoryTemplateFolder() {
        if (workingDirectoryTemplateFolder == null) return null;
        return Paths.get(workingDirectoryTemplateFolder);
    }
}
