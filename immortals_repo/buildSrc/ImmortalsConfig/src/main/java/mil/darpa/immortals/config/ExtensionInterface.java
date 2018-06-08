package mil.darpa.immortals.config;

import java.nio.file.Path;

/**
 * Created by awellman@bbn.com on 5/9/18.
 */
public interface ExtensionInterface {
    
    public String getIdentifier();
    
    public String getExePath();
    
    public Path getWorkingDirectoryTemplateFolder();
}
