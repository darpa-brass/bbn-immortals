package mil.darpa.immortals.core.das.adaptationtargets.building;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.das.ImmortalsProcessBuilder;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 1/25/18.
 */
public class AdaptationTargetBuildInstance implements AdaptationTargetInterface {

    private final Logger logger;

    private String adaptationIdentifier;

    private String instanceIdentifier;

    private AdaptationTargetBuildBase buildEnvironment;

    private GradleBuildFileHelper gradleHelper;

    public AdaptationTargetBuildInstance(String adaptationIdentifier, AdaptationTargetBuildBase buildEnvironment) {
        this.adaptationIdentifier = adaptationIdentifier;
        this.instanceIdentifier = buildEnvironment.getTargetIdentifier() + adaptationIdentifier;
        this.buildEnvironment = buildEnvironment;
        this.gradleHelper = new GradleBuildFileHelper(this);

        logger = LoggerFactory.getLogger("ApplicationBuildInstance(id=" + instanceIdentifier + ")");

        logger.debug("ABB(id=" + buildEnvironment.getTargetIdentifier() + ") -> ABI(adaptationIdentifier=" +
                adaptationIdentifier + ", id=" + instanceIdentifier + ")");
    }

    /**
     * @return The adaptation identifier associated with this build instance
     */
    public String getAdaptationIdentifier() {
        return adaptationIdentifier;
    }

    /**
     * @return An identifier that can be used to uniquely identify this build instance
     */
    public String getInstanceIdentifier() {
        return instanceIdentifier;
    }

    @Override
    public int getSettleTimeMS() {
        return buildEnvironment.getSettleTimeMS();
    }

    @Override
    public DeploymentTarget getDeploymentTarget() {
        return buildEnvironment.getDeploymentTarget();
    }

    @Override
    public String getDeploymentTargetVersion() {
        return buildEnvironment.getDeploymentTargetVersion();
    }

    @Override
    public synchronized Path getBuildRoot() {
        Path buildRoot;

        buildRoot = ImmortalsConfig.getInstance().globals.getAdaptationWorkingDirectory(adaptationIdentifier)
                .resolve(buildEnvironment.getProjectRepoPath()).toAbsolutePath();
        if (!Files.exists(buildRoot)) {
            try {
                Files.createDirectories(buildRoot);

                // Assuming local filesystem for now
                Path sourceDir = Paths.get(buildEnvironment.getRepoLocation()).resolve(buildEnvironment.getProjectRepoPath());

                FileUtils.copyDirectory(sourceDir.toFile(), buildRoot.toFile(), true);

            } catch (IOException e) {
                ImmortalsErrorHandler.reportFatalException(e);
            }
        }
        return buildRoot.toAbsolutePath();
    }

    @Override
    public String getExecutionMainMethod() {
        return buildEnvironment.getExecutionMainMethod();
    }

    @Override
    public String getExecutionPackageIdentifier() {
        return buildEnvironment.getExecutionPackageIdentifier();
    }

    public Path getExecutablePath() {
        return getBuildRoot().resolve(buildEnvironment.getExecutableFile());
    }

    public Map<File, String> getDeploymentFileMap() {
        HashMap<File, String> rval = new HashMap<>();

        Map<String, String> dfm = buildEnvironment.getDeploymentFileMap();
        for (Map.Entry<String, String> entry : dfm.entrySet()) {
            if (entry.getKey().startsWith("/")) {
                rval.put(new File(entry.getKey()), entry.getValue());
            } else {
                rval.put(new File(getBuildRoot().resolve(entry.getKey()).toString()), entry.getValue());
            }
        }
        return rval;
    }

    @Override
    public BuildPlatform getBuildPlatform() {
        return buildEnvironment.getBuildPlatform();
    }

    @Override
    public Path getBuildToolPath() {
        return buildEnvironment.getBuildToolPath();
    }

    @Override
    public String[] getBuildToolBuildParameters() {
        return buildEnvironment.getBuildToolBuildParameters();
    }

    @Override
    public String[] getBuildToolValidationParameters() {
        return buildEnvironment.getBuildToolValidationParameters();
    }


    @Override
    public String[] getBuildToolPublishParameters() {
        return buildEnvironment.getBuildToolPublishParameters();
    }

    /**
     * @return The source subdirectory. You probably shouldn't use this directly unless it is required for an extension
     */
    public String getProjectSourceSubdirectory() {
        return buildEnvironment.getSourceSubdirectory();
    }

    @Override
    public Path getSourceRoot() {
        return getBuildRoot().resolve(buildEnvironment.getSourceSubdirectory());
    }

    @Override
    public Path getBuildFilePath() {
        return getBuildRoot().resolve(buildEnvironment.getBuildFile());
    }

    /**
     * @return The path that contains the results of validation tests
     */
    public Path getTestResultsPath() {
        return getBuildRoot().resolve(buildEnvironment.getBuildToolTestResultsSubdirectory());
    }

    /**
     * Updates the original dependency of the provided build instance with the new one
     *
     * @param buildInstance The new instance of an already in use library to update to
     * @throws IOException If has issues modifying the build file
     */
    public void updateBuildScriptDependency(AdaptationTargetBuildInstance buildInstance) throws IOException {
        gradleHelper.replaceDependency(buildInstance.buildEnvironment.getOwnDependencyCoordinates(),
                buildInstance.getOwnDependencyCoordinates());
        gradleHelper.save();
    }

    @Override
    public String getOwnDependencyCoordinates() {
        if (buildEnvironment.getPublishArtifactId() == null) {
            return null;
        }
        return getPublishGroupId() + ":" + getPublishArtifactId() + ":" + getPublishVersion();
    }

    @Override
    public String getPublishGroupId() {
        return buildEnvironment.getPublishGroupId();

    }

    @Override
    public String getPublishArtifactId() {
        return buildEnvironment.getPublishArtifactId() + "-" + adaptationIdentifier;

    }

    @Override
    public String getPublishVersion() {
        return buildEnvironment.getPublishVersion();
    }

    /**
     * Cleans and builds the application instance (`gradle clean build`)
     *
     * @return Whether or not it was build successfully
     * @throws IOException          If it fails to start the process
     * @throws InterruptedException If it is interrupted while waiting for the process to finish
     */
    public boolean executeCleanAndBuild() throws IOException, InterruptedException {
        return gradleHelper.executeCleanAndBuild();
    }

    /**
     * Cleans the application and runs the standard test suite (`gradle clean test`)
     *
     * @return Whether or not it was build successfully
     * @throws IOException          If it fails to start the process
     * @throws InterruptedException If it is interrupted while waiting for the process to finish
     */
    public boolean executeCleanAndTest() throws IOException, InterruptedException {
        return gradleHelper.executeCleanAndTest();
    }

    /**
     * Executes `gradle clean build publish` DO NOT Run this on something that does not publish an artifact!
     *
     * @return Whether or not it was successful.
     * @throws IOException          If it fails to start the process
     * @throws InterruptedException If it is interrupted while waiting for the process to finish
     */
    public boolean executeCleanBuildAndPublish() throws IOException, InterruptedException {
        return gradleHelper.executeCleanBuildAndPublish();
    }

    @Override
    public String getTargetIdentifier() {
        return buildEnvironment.getTargetIdentifier();
    }


    public static void main(String[] args) {
        GradleKnowledgeBuilder gkr = new GradleKnowledgeBuilder();
        try {
            gkr.buildKnowledge(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        // Write the deployment model for ingestion
        try {
            String deploymentModelText = "";
            Path deploymentModelTarget = ImmortalsConfig.getInstance().extensions.getProducedTtlOutputDirectory().resolve("deployment_model.ttl");
            Files.write(deploymentModelTarget, deploymentModelText.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Get the ingestion directory to provide to the knowledge repo
        ImmortalsConfig.getInstance().globals.getTtlIngestionDirectory();


        // Get the adaptation identifier
        String adaptationIdentifier = "adaptation" + Long.toString(System.currentTimeMillis()).substring(0, 10);

        // Applications and Libraries use the same build management classes

        // You can get the base (which should NEVER be modified) for things such as baseline analysis like so
        AdaptationTargetBuildBase takServerDataManagerBase = gkr.getBuildBase("TakServerDataManager");
        AdaptationTargetBuildBase martiBase = gkr.getBuildBase("Marti");


        // Or build instances which are meant to be modified
        AdaptationTargetBuildInstance martiInstance = gkr.getBuildInstance("Marti", adaptationIdentifier);
        AdaptationTargetBuildInstance takServerDataManagerInstance = gkr.getBuildInstance("TakServerDataManager", adaptationIdentifier);

        // From here, see {@link AdaptationTargetBuildInstance} and {@link AdaptationTargetBuildInstance} to see option explanations
        // Anything that returns a path is the full path to what you need (which helps minimize ambiguity)

        // Get the source directory for TakServerDataManager and have fun
        takServerDataManagerInstance.getSourceRoot();

        // Build and publish the new TakServerDataManager instance
        try {

            boolean success = takServerDataManagerInstance.executeCleanBuildAndPublish();
            if (success) {
                System.out.println("The TakServerDataManager publish successfully!");
            } else {
                System.out.println("The TakServerDataManager publish FAILED!");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


        // Update the dependency in Marti for the new TakServerDataManager instance
        try {
            martiInstance.updateBuildScriptDependency(takServerDataManagerInstance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Rebuild Marti.
        try {
            boolean success = martiInstance.executeCleanAndBuild();
            if (success) {
                System.out.println("The Marti build finished successfully!");
            } else {
                System.out.println("The Marti build FAILED!");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            boolean success = takServerDataManagerInstance.executeCleanAndTest();
            if (success) {
                System.out.println("The TakServerDataManager validation finished successfully!");
            } else {
                System.out.println("The TakServerDataManager validation FAILED!");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
