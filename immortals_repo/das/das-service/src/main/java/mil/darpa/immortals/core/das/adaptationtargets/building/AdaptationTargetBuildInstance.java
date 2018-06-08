package mil.darpa.immortals.core.das.adaptationtargets.building;

import mil.darpa.immortals.analysis.adaptationtargets.BuildPlatform;
import mil.darpa.immortals.analysis.adaptationtargets.DeploymentTarget;
import mil.darpa.immortals.analysis.adaptationtargets.ImmortalsGradleProjectData;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.TestCaseReport;
import mil.darpa.immortals.core.api.TestCaseReportSet;
import mil.darpa.immortals.core.das.adaptationtargets.testing.*;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by awellman@bbn.com on 1/25/18.
 */
public class AdaptationTargetBuildInstance implements AdaptationTargetInterface {

    private final Logger logger;

    private String adaptationIdentifier;

    private String instanceIdentifier;

    private AdaptationTargetBuildBase base;

    private GradleBuildFileHelper gradleHelper;

    public AdaptationTargetBuildInstance(String adaptationIdentifier, AdaptationTargetBuildBase base) {
        this.adaptationIdentifier = adaptationIdentifier;
        this.instanceIdentifier = base.getTargetName() + "-" + adaptationIdentifier;
        this.base = base;
        this.gradleHelper = new GradleBuildFileHelper(this);

        logger = LoggerFactory.getLogger("ApplicationBuildInstance(id=" + instanceIdentifier + ")");

        logger.debug("ABB(id=" + base.getTargetName() + ") -> ABI(adaptationIdentifier=" +
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
    public Integer getExecutionStartSettleTimeMS() {
        if (!canExecute()) return null;
        return base.getExecutionStartSettleTimeMS();
    }

    @Override
    public DeploymentTarget getDeploymentTarget() {
        return base.getDeploymentTarget();
    }

    @Override
    public String getDeploymentTargetVersion() {
        return base.getDeploymentTargetVersion();
    }

    @Override
    public boolean canExecute() {
        return base.canExecute();
    }

    @Override
    public synchronized Path getBuildRoot() {
        Path buildRoot;

        buildRoot = ImmortalsConfig.getInstance().globals.getAdaptationWorkingDirectory(adaptationIdentifier)
                .resolve(base.getTargetName()).toAbsolutePath();
        if (!Files.exists(buildRoot)) {
            try {
                Files.createDirectories(buildRoot);

                // Assuming local filesystem for now
                Path sourceDir = base.getBuildRoot();

                FileUtils.copyDirectory(sourceDir.toFile(), buildRoot.toFile(), true);

            } catch (IOException e) {
                ImmortalsErrorHandler.reportFatalException(e);
            }
        }
        return buildRoot.toAbsolutePath();
    }

    @Override
    public String getExecutionMainMethod() {
        if (getRawBaseProjectData().getExecution() == null) return null;
        return base.getExecutionMainMethod();
    }

    @Override
    public String getExecutionPackageIdentifier() {
        if (getRawBaseProjectData().getExecution() == null) return null;
        return base.getExecutionPackageIdentifier();
    }

    @Override
    public Path getExecutablePath() {
        if (getRawBaseProjectData().getExecution() == null) return null;
        return getBuildRoot().resolve(getRawBaseProjectData().getExecution().getExecutableFilename());
    }

    @Override
    public Map<File, String> getExecutionDeploymentFileMap() {
        if (getRawBaseProjectData().getExecution() == null) return null;
        HashMap<File, String> rval = new HashMap<>();

        Map<String, String> dfm = getRawBaseProjectData().getExecution().getExecutionFileMap();
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
        return base.getBuildPlatform();
    }

    @Override
    public boolean canTest() {
        return base.canTest();
    }

    @Override
    public Path getBuildToolPath() {
        return base.getBuildToolPath();
    }

    @Override
    public String[] getBuildToolBuildParameters() {
        return base.getBuildToolBuildParameters();
    }

    @Override
    public String[] getTestBuildToolParameters() {
        return base.getTestBuildToolParameters();
    }


    @Override
    public String[] getPublishBuildToolParameters() {
        return base.getPublishBuildToolParameters();
    }

    @Override
    public ImmortalsGradleProjectData getRawBaseProjectData() {
        return base.getRawBaseProjectData();
    }

    @Override
    public Path getSourceRoot() {
        return getBuildRoot().resolve(getRawBaseProjectData().getSourceSubdirectory());
    }

    @Override
    public Path getBuildFilePath() {
        return getBuildRoot().resolve(getRawBaseProjectData().getBuildFile());
    }

    @Override
    public Path getTestResultsPath() {
        if (getRawBaseProjectData().getTesting() == null) return null;
        return getBuildRoot().resolve(getRawBaseProjectData().getTesting().getTestResultXmlSubdirectory());
    }
    
    @Override
    public Path getTestCoverageReportXmlFileSubpath() {
        if (getRawBaseProjectData().getTesting() == null) return null;
        return getBuildRoot().resolve(base.getTestCoverageReportXmlFileSubpath());
    }

    /**
     * Updates the original dependency of the provided build instance with the new one
     *
     * @param buildInstance The new instance of an already in use library to update to
     * @throws IOException If has issues modifying the build file
     */
    public void updateBuildScriptDependency(AdaptationTargetBuildInstance buildInstance) throws IOException {
        gradleHelper.replaceDependency(buildInstance.base.getPublishDependencyCoordinates(),
                buildInstance.getPublishDependencyCoordinates());
        gradleHelper.save();
    }

    /**
     * Updates the original dependency of the provided build instance with the new one
     *
     * @param originalCoordinates the coordinates to be replaced
     * @param newCoordinates the coordinates to replace with
     * @throws IOException If has issues modifying the build file
     */
    public void updateBuildScriptDependency(@Nonnull String originalCoordinates, @Nonnull String newCoordinates) throws IOException {
        gradleHelper.replaceDependency(originalCoordinates, newCoordinates);
        gradleHelper.save();
    }
    
    public void updateBuildScriptDependency(@Nonnull String originalCoordinates, @Nonnull Path newJar) {
        gradleHelper.replaceDependency(originalCoordinates, newJar);
    }

    @Override
    public String getPublishDependencyCoordinates() {
        if (base.getPublishArtifactId() == null) {
            return null;
        }
        return getPublishGroupId() + ":" + getPublishArtifactId() + ":" + getPublishVersion();
    }

    @Override
    public boolean canPublish() {
        return base.canPublish();
    }

    @Override
    public String getPublishGroupId() {
        return base.getPublishGroupId();

    }

    @Override
    public String getPublishArtifactId() {
        return base.getPublishArtifactId() + "-" + adaptationIdentifier;

    }

    @Override
    public String getPublishVersion() {
        return base.getPublishVersion();
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
     * @return The test results, or null if it is not testable
     * @throws IOException          If it fails to start the process
     * @throws InterruptedException If it is interrupted while waiting for the process to finish
     */
    public TestCaseReportSet executeCleanAndTest(@Nullable Map<String, Set<String>> testFunctionalityMap) throws IOException, InterruptedException, DocumentException {
        return executeCleanAndTest(testFunctionalityMap, null);
    }

    public TestCaseReportSet executeCleanAndTest(@Nullable Map<String, Set<String>> testFunctionalityMap, @Nullable Collection<String> testIdentifiers) throws IOException, InterruptedException, DocumentException {
        Boolean rval = gradleHelper.executeCleanAndTest(testIdentifiers);
        if (rval == null) {
            return null;
        }
        return XmlParser.getTestResultsFromFlatDirectory(getTestResultsPath().toFile(), base.getTargetIdentifier(), testFunctionalityMap);
    }
    
    public ClassFileCoverageSet executeCleanTestAndGetCoverage(@Nullable Collection<String> testIdentifiers) throws Exception {
        Boolean rval = gradleHelper.executeCleanTestAndGetCoverage(testIdentifiers);
        if (rval == null || !Files.exists(getTestCoverageReportXmlFileSubpath())) {
            return null;
        }
        return XmlParser.createClassFileCoverageReportsFromFile(getTestCoverageReportXmlFileSubpath().toFile());
    }

    /**
     * Executes `gradle clean build publish` DO NOT Run this on something that does not publish an artifact!
     *
     * @return Whether or not it was successful. Returns null if it is not publishable
     * @throws IOException          If it fails to start the process
     * @throws InterruptedException If it is interrupted while waiting for the process to finish
     */
    public boolean executeCleanBuildAndPublish() throws IOException, InterruptedException {
        return gradleHelper.executeCleanBuildAndPublish();
    }

    @Override
    public String getTargetName() {
        return base.getTargetName();
    }
    
    @Override
    public String getTargetIdentifier() {
        return base.getTargetIdentifier();
    }


    public static void main(String[] args) {
        try {
            // Write the deployment model for ingestion
            String deploymentModelText = "";
            Path deploymentModelTarget = ImmortalsConfig.getInstance().extensions.getProducedTtlOutputDirectory().resolve("deployment_model.ttl");
            Files.write(deploymentModelTarget, deploymentModelText.getBytes());

            // Get the ingestion directory to provide to the knowledge repo
            ImmortalsConfig.getInstance().globals.getTtlIngestionDirectory();


            // Get the adaptation identifier
            String adaptationIdentifier = "adaptation" + Long.toString(System.currentTimeMillis()).substring(0, 10);

            // Applications and Libraries use the same build management classes

            // You can get the base (which should NEVER be modified) for things such as baseline analysis like so
            AdaptationTargetBuildBase takServerDataManagerBase = GradleKnowledgeBuilder.getBuildBase("TakServerDataManager");
            AdaptationTargetBuildBase martiBase = GradleKnowledgeBuilder.getBuildBase("Marti");


            // Or build instances which are meant to be modified
            AdaptationTargetBuildInstance martiInstance = GradleKnowledgeBuilder.getBuildInstance("Marti", adaptationIdentifier);
            AdaptationTargetBuildInstance takServerDataManagerInstance = GradleKnowledgeBuilder.getBuildInstance("TakServerDataManager", adaptationIdentifier);

            // From here, see {@link AdaptationTargetBuildInstance} and {@link AdaptationTargetBuildInstance} to see option explanations
            // Anything that returns a path is the full path to what you need (which helps minimize ambiguity)

            // Get the source directory for TakServerDataManager and have fun
            takServerDataManagerInstance.getSourceRoot();

            // Build and publish the new TakServerDataManager instance
            boolean success = takServerDataManagerInstance.executeCleanBuildAndPublish();
            if (success) {
                System.out.println("The TakServerDataManager publish successfully!");
            } else {
                System.out.println("The TakServerDataManager publish FAILED!");
            }

            // Update the dependency in Marti for the new TakServerDataManager instance
            martiInstance.updateBuildScriptDependency(takServerDataManagerInstance);

            // Rebuild Marti.
            success = martiInstance.executeCleanAndBuild();
            if (success) {
                System.out.println("The Marti build finished successfully!");
            } else {
                System.out.println("The Marti build FAILED!");
            }

            Set<TestCaseReport> tsr = takServerDataManagerInstance.executeCleanAndTest(null);
            if (tsr != null) {
                System.out.println("The TakServerDataManager validation finished with results.");

            } else {
                System.out.println("The TakServerDataManager validation FAILED!");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
