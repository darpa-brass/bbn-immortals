package mil.darpa.immortals.config.extensions;

import mil.darpa.immortals.config.GlobalsConfig;
import mil.darpa.immortals.config.ImmortalsConfig;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by awellman@bbn.com on 1/30/18.
 */
public class HddRassConfiguration {

    private final String identifier = "hddrass";

    private final String jarPath = "extensions/osu/hddrass/build/libs/hddRASS-1.0-SNAPSHOT.jar";
    
    private final String gradleBuildFile = "extensions/osu/hddrass/build.gradle";

    public Path getJarPath() {
        return ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve(jarPath);
    }

    public String getIdentifier() {
        return identifier;
    }

    public Path getExecutionWorkingDirectory(@Nonnull String adaptationIdentifier) {
        return ImmortalsConfig.getInstance().globals.getAdaptationComponentWorkingDirectory(adaptationIdentifier, identifier);
    }

    public Path getWorkingDirectory() {
        return Paths.get(GlobalsConfig.mkworkingdir("_" + identifier));
    }
    
    public String getGradleBuildFile() {
        return gradleBuildFile;
    }
}
