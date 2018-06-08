package mil.darpa.immortals.config.extensions;

import mil.darpa.immortals.config.GitCloneInterface;
import mil.darpa.immortals.config.GlobalsConfig;
import mil.darpa.immortals.config.ImmortalsConfig;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by awellman@bbn.com on 5/9/18.
 */
public class PartialLibraryUpdatesConfiguration implements GitCloneInterface {

    private final String identifier = "partiallibraryupgrade";
    private final String gitRepositoryUrl = "https://github.com/yijiufly/thirdPartyLibAnalysis";
    private final String targetClonePath = GlobalsConfig.staticImmortalsRoot.resolve("extensions/ucr/thirdPartyLibAnalysis").toString();
    public final String exePath = Paths.get(targetClonePath).resolve("yuesLib.py").toString();

    public final String workingDirectoryTemplateFolder = targetClonePath;

    @Override
    public String getGitRepositoryUrl() {
        return gitRepositoryUrl;
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
    public Path getWorkingDirectoryTemplateFolder() {
        return Paths.get(workingDirectoryTemplateFolder);
    }

    public Path getExecutionWorkingDirectory(@Nonnull String adaptationIdentifier) {
        return ImmortalsConfig.getInstance().globals.getAdaptationComponentWorkingDirectory(adaptationIdentifier, identifier);
    }
    
    @Override
    public Path getTargetClonePath() {
        return Paths.get(targetClonePath);
    }
}
