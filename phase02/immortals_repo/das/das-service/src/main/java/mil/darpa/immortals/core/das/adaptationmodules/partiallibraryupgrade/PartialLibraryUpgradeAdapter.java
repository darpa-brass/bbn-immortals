package mil.darpa.immortals.core.das.adaptationmodules.partiallibraryupgrade;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;
import mil.darpa.immortals.core.das.adaptationmodules.IAdaptationModule;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.core.das.sparql.deploymentmodel.DeterminePartialLibraryUpgradeApplicability;
import mil.darpa.immortals.das.ImmortalsProcessBuilder;
import mil.darpa.immortals.das.context.DasAdaptationContext;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by awellman@bbn.com on 4/3/18.
 */
public class PartialLibraryUpgradeAdapter implements IAdaptationModule {
    private String adaptationIdentifier;
    private List<DeterminePartialLibraryUpgradeApplicability.PartialLibraryUpgradeCandidate> upgradeCandidates;

    public PartialLibraryUpgradeAdapter() {

    }

    @Override
    public boolean isApplicable(DasAdaptationContext context) throws Exception {
        this.adaptationIdentifier = context.getAdaptationIdentifer();
        upgradeCandidates = DeterminePartialLibraryUpgradeApplicability.select(context);
        return (upgradeCandidates != null && upgradeCandidates.size() > 0);
    }

    @Override
    public void apply(DasAdaptationContext context) throws Exception {
        for (DeterminePartialLibraryUpgradeApplicability.PartialLibraryUpgradeCandidate candidate : upgradeCandidates) {
            AdaptationTargetBuildInstance instance = GradleKnowledgeBuilder.getBuildInstance(
                    candidate.applicationIdentifier, context.getAdaptationIdentifer());

            Path workingDirectory = ImmortalsConfig.getInstance().extensions.partiallibraryupgrade.getExecutionWorkingDirectory(context.getAdaptationIdentifer());

            Path oldLibrary = fetchLibrary(
                    candidate.vulnerableLibraryRepo,
                    candidate.getVulnerableLibraryCoordinate(),
                    workingDirectory
            );

            Path newLibrary = fetchLibrary(
                    candidate.fixedLibraryRepo,
                    candidate.getFixedLibraryCoordinate(),
                    workingDirectory
            );

            PartialLibraryUpgradeInitializationObject init =
                    new PartialLibraryUpgradeInitializationObject(oldLibrary.toString(), newLibrary.toString());

            PartialLibraryUpgradeExecuter exe = new PartialLibraryUpgradeExecuter(context, init);
            Process p = exe.execute();
            p.waitFor();
            int code = p.exitValue();

            DasOutcome outcome;
            if (code == 0) {
                outcome = DasOutcome.SUCCESS;

                instance.updateBuildScriptDependency(
                        candidate.getVulnerableLibraryCoordinate(),
                        workingDirectory.resolve("patched.jar")
                );

            } else {
                outcome = DasOutcome.ERROR;
            }
            AdaptationDetails update = new AdaptationDetails(
                    getClass().getName(),
                    outcome,
                    context.getAdaptationIdentifer()
            );
            context.submitAdaptationStatus(update);
        }
    }

    public synchronized Path fetchLibrary(@Nonnull String repositoryUrl, @Nonnull String coordinate, @Nonnull Path targetFolder) throws Exception {
        Path targetPath = targetFolder.resolve(coordinate.replaceAll(":", "-") + ".jar");

        String[] mvnFetchCommand = {"mvn", "dependency:get",
                "-DrepoUrl=" + repositoryUrl,
                "-Dartifact=" + coordinate,
                "-Ddest=" + targetPath.toString()};

        ImmortalsProcessBuilder pb = new ImmortalsProcessBuilder(adaptationIdentifier, "partiallibraryupgrade");
        pb.command(mvnFetchCommand);
        Process p = pb.start();
        p.waitFor(60000, TimeUnit.MILLISECONDS);

        if (p.exitValue() != 0) {
            throw new IOException("Failed to download dependency '" + coordinate + "'!");
        }
        return targetPath;
    }
}
