package mil.darpa.immortals.config;

import java.nio.file.Path;
import java.util.HashMap;

/**
 * Created by awellman@bbn.com on 12/19/17.
 */
public interface AppConfigInterface extends ExtensionInterface {
    public boolean isUserManaged();
    
    public long getStartupTimeMS();

    public String getIdentifier();

    public String getExePath();
    
    public Path getWorkingDirectoryTemplateFolder();

    public String getWorkingDirectory();
    
    public String[] getInterpreterParameters();

    public String[] getParameters();

    public HashMap<String, String> getEnvironmentVariables();
    
    public String getReadyStdoutLineRegexPattern();

    public boolean isShutdownEverythingOnTermination();
}
