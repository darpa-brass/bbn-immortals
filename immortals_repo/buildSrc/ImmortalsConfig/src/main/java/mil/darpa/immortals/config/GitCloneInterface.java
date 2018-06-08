package mil.darpa.immortals.config;

import java.nio.file.Path;

/**
 * Created by awellman@bbn.com on 5/9/18.
 */
public interface GitCloneInterface extends ExtensionInterface {
    
    public String getGitRepositoryUrl();
    
    public Path getTargetClonePath();
}
