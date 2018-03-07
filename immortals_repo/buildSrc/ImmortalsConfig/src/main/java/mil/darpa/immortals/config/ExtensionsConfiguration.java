package mil.darpa.immortals.config;

import mil.darpa.immortals.config.extensions.AqlBrassConfiguration;
import mil.darpa.immortals.config.extensions.CastorConfiguration;
import mil.darpa.immortals.config.extensions.HddRassConfiguration;
import mil.darpa.immortals.config.extensions.KnowledgeRepoGradlePluginConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by awellman@bbn.com on 1/18/18.
 */
public class ExtensionsConfiguration {

    public static final Path staticProducedTtlOutputDirectory = StaticHelper.resolveNewDirectoryValue(
            "extensions.ttlAnalysisDirectory",
            GlobalsConfig.staticTtlIngestionDirectory.resolve("_ANALYSIS"));

    public final CastorConfiguration castor = new CastorConfiguration();
    public final KnowledgeRepoGradlePluginConfiguration krgp = new KnowledgeRepoGradlePluginConfiguration();
    public final HddRassConfiguration hddrass = new HddRassConfiguration();
    public final AqlBrassConfiguration aqlbrass = new AqlBrassConfiguration();

    private String producedTtlOutputDirectory = staticProducedTtlOutputDirectory.toString();

    public ExtensionsConfiguration() {

    }

    public Path getProducedTtlOutputDirectory() {
        return Paths.get(producedTtlOutputDirectory);
    }

}
