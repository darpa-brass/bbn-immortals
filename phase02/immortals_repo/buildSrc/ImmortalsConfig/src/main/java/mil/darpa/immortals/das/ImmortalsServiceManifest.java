package mil.darpa.immortals.das;

import mil.darpa.immortals.config.ExtensionInterface;
import mil.darpa.immortals.config.GitCloneInterface;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.config.extensions.MavenArtifactInterface;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by awellman@bbn.com on 3/5/18.
 */
public enum ImmortalsServiceManifest {
    fuseki,
    knowledgerepo,
    voltdb,
    aqlbrass,
    dasservice,
    testadapter,
    partiallibraryupgrade;

    public static final String displayString = "[fuseki|knowledgerepo|voltdb|aqlbrass|dasservice|testadapter]";

    public ExtensionInterface getConfig() {
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
            case voltdb:
                return ic.extensions.voltdb;
            case aqlbrass:
                return ic.extensions.aqlbrass;
            case partiallibraryupgrade:
                return ic.extensions.partiallibraryupgrade;
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
            case voltdb:
                return false;
            case aqlbrass:
                return ic.debug.isUseMockAqlBrass();
            case partiallibraryupgrade:
                return false;
        }
        throw new RuntimeException("Unexpected service enum '" + this.name() + "'!");
    }

    public static synchronized void deploy() throws InterruptedException, IOException {
        for (ImmortalsServiceManifest service : ImmortalsServiceManifest.values()) {
            ExtensionInterface config = service.getConfig();

            if (config instanceof MavenArtifactInterface) {
                Path p = Paths.get(config.getExePath());
                if (!Files.exists(p)) {
                    throw new RuntimeException("Could not find file '" + p.toString() + "'!. Are you sure it is being fetched as a maven dependency in the extensions project?");
                }
            }

            if (config instanceof GitCloneInterface) {
                GitCloneInterface gitConfig = (GitCloneInterface) config;

                if (Files.exists(gitConfig.getTargetClonePath().resolve(".git"))) {
                    // If a git project appears to exist, update it
                    String[] gitUpdateCommand = {"git", "pull", "origin", "master"};
                    ProcessBuilder pb = new ProcessBuilder(gitUpdateCommand);
                    pb.directory(gitConfig.getTargetClonePath().toFile());
                    System.out.println("EXEC: `" + pb.command().stream().collect(Collectors.joining(" ")) + "`");
                    Process p = pb.start();
                    p.waitFor(60000, TimeUnit.MILLISECONDS);

                    if (p.exitValue() != 0) {
                        throw new RuntimeException("'git pull origin master' on '" + gitConfig.getTargetClonePath().toString() + "' failed!");
                    }

                } else {
                    // Otherwise, clone it as a new project
                    String[] gitCloneCommand = {"git", "clone", gitConfig.getGitRepositoryUrl(),
                            gitConfig.getTargetClonePath().toString()};
                    ProcessBuilder pb = new ProcessBuilder(gitCloneCommand);
                    System.out.println("EXEC: `" + pb.command().stream().collect(Collectors.joining(" ")) + "`");
                    Process p = pb.start();
                    p.waitFor(60000, TimeUnit.MILLISECONDS);

                    if (p.exitValue() != 0) {
                        throw new IOException("Failed to git clone dependency '" + config.getIdentifier() + "'!");
                    }
                }
            }
        }
    }
}
