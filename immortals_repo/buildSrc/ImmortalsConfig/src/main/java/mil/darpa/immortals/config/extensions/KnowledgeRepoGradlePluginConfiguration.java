package mil.darpa.immortals.config.extensions;

import mil.darpa.immortals.config.ExtensionsConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by awellman@bbn.com on 1/30/18.
 */
public class KnowledgeRepoGradlePluginConfiguration {

    private final String identifier = "krgp";

    private boolean performBytecodeAnalysis = true;

    // NEVER AUTO-CREATE THIS DIRECTORY!!!
    private String ttlTargetDirectory = ExtensionsConfiguration.staticProducedTtlOutputDirectory.resolve("_" + identifier).toAbsolutePath().toString();

    public KnowledgeRepoGradlePluginConfiguration() {

    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isPerformBytecodeAnalysis() {
        return performBytecodeAnalysis;
    }

    public Path getTtlTargetDirectory() {
        return Paths.get(ttlTargetDirectory);
    }
}
