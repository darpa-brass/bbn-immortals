package mil.darpa.immortals.das;

import mil.darpa.immortals.config.AppConfigInterface;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.extensions.MavenArtifactInterface;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Created by awellman@bbn.com on 3/5/18.
 */
public enum ImmortalsServiceManifest {
    fuseki,
    knowledgerepo,
    dasservice,
    testadapter,
    aqlbrass;

    public static final String displayString = "[fuseki|knowledgerepo|dasservice|testadapter|aqlbrass]";

    public AppConfigInterface getConfig() {
        ImmortalsConfig ic = ImmortalsConfig.getInstance();
        switch (this) {
            case dasservice:
                return ic.dasService;
            case fuseki:
                return ic.fuseki;
            case knowledgerepo:
                return ic.knowledgeRepoService;
            case testadapter:
                return ic.testAdapter;
            case aqlbrass:
                return ic.extensions.aqlbrass;
        }
        throw new RuntimeException("Unexpected service enum '" + this.name() + "'!");
    }

    public boolean useMock() {
        ImmortalsConfig ic = ImmortalsConfig.getInstance();
        switch (this) {
            case dasservice:
                return ic.debug.isUseMockDas();
            case fuseki:
                return ic.debug.isUseMockFuseki();
            case knowledgerepo:
                return ic.debug.isUseMockKnowledgeRepository();
            case testadapter:
                return ic.debug.isUseMockTestAdapter();
            case aqlbrass:
                return ic.debug.isUseMockAqlBrass();
        }
        throw new RuntimeException("Unexpected service enum '" + this.name() + "'!");
    }

    public static synchronized void deploy() throws InterruptedException, IOException {
        for (ImmortalsServiceManifest service : ImmortalsServiceManifest.values()) {
            AppConfigInterface config = service.getConfig();

            if (config instanceof MavenArtifactInterface) {
                if (!Files.exists(Paths.get(config.getExePath()))) {
                    MavenArtifactInterface mvnConfig = (MavenArtifactInterface) config;
                    String[] mvnFetchCommand = {"mvn", "dependency:get",
                            "-DrepoUrl=" + mvnConfig.getMavenRepositoryUrl(),
                            "-Dartifact=" + mvnConfig.getMavenFullDependencyCoordinate(),
                            "-Ddest=" + mvnConfig.getExePath()};

                    ProcessBuilder pb = new ProcessBuilder(mvnFetchCommand);
                    Process p = pb.start();
                    p.waitFor(60000, TimeUnit.MILLISECONDS);

                    if (p.exitValue() != 0) {
                        throw new IOException("Failed to download dependency '" + config.getIdentifier() + "'!");
                    }
                }
            }
        }
    }
}
