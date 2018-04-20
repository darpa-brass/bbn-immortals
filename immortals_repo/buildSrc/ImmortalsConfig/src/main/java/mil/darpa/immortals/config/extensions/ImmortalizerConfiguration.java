package mil.darpa.immortals.config.extensions;

import mil.darpa.immortals.config.GlobalsConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by awellman@bbn.com on 3/29/18.
 */
public class ImmortalizerConfiguration {

    private final String identifier = "immortalizer";

    private boolean performBuildFileAnalysis = true;
    
    private boolean performKrgpBytecodeAnalysis = true;

    private boolean performKrgpCompleteGradleTaskAnalysis = true;
    
    private boolean performTestCoverageAnalysis = true;

    public String getIdentifier() {
        return identifier;
    }

    public boolean isPerformBuildFileAnalysis() {
        return performBuildFileAnalysis;
    }

    private String producedDataTargetFile = GlobalsConfig.staticImmortalsRoot.resolve("ARTIFACT_DATA.json").toString();

    public Path getProducedDataTargetFile() {
        return Paths.get(producedDataTargetFile);
    }
    
    public boolean isPerformKrgpBytecodeAnalysis() {
        return performKrgpBytecodeAnalysis;
    }
    
    public boolean isPerformKrgpCompleteGradleTaskAnalysis() {
        return performKrgpCompleteGradleTaskAnalysis;
    }

    public boolean isPerformTestCoverageAnalysis() {
        return performTestCoverageAnalysis;
    }
}
