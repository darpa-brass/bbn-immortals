package mil.darpa.immortals.core.das.adaptationtargets.building;

import mil.darpa.immortals.analysis.adaptationtargets.BuildPlatform;
import mil.darpa.immortals.analysis.adaptationtargets.DeploymentTarget;
import mil.darpa.immortals.analysis.adaptationtargets.ImmortalsGradleProjectData;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.adaptationtargets.testing.ClassFileCoverageSet;
import mil.darpa.immortals.core.api.TestCaseReportSet;
import mil.darpa.immortals.core.das.adaptationtargets.testing.XmlParser;
import mil.darpa.immortals.das.ImmortalsProcessBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by awellman@bbn.com on 1/25/18.
 */
public class AdaptationTargetBuildBase implements AdaptationTargetInterface {

    private String buildToolPath;

    private ImmortalsGradleProjectData projectData;

    public AdaptationTargetBuildBase(@Nonnull ImmortalsGradleProjectData projectData) {
        this.projectData = projectData;
        this.buildToolPath = projectData.getBuildPlatform() == BuildPlatform.GRADLE ?
                ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("gradlew").toString() : null;
    }

    @Override
    public boolean canPublish() {
        return projectData.getPublishing() != null;
    }

    @Override
    public String getPublishGroupId() {
        if (projectData.getPublishing() == null) return null;
        return projectData.getPublishing().getGroupId();
    }

    @Override
    public String getPublishArtifactId() {
        if (projectData.getPublishing() == null) return null;
        return projectData.getPublishing().getArtifactId();
    }

    @Override
    public String getPublishVersion() {
        if (projectData.getPublishing() == null) return null;
        return projectData.getPublishing().getVersion();
    }

    @Override
    public String getPublishDependencyCoordinates() {
        if (projectData.getPublishing() == null) return null;
        return projectData.getPublishing().getPublishCoordinates();
    }

    @Override
    public Path getTestCoverageReportXmlFileSubpath() {
        if (projectData.getTesting() == null) return null;
        return getBuildRoot().resolve(projectData.getTesting().getTestCoverageReportXmlFileSubpath());
    }

    @Override
    public ImmortalsGradleProjectData getRawBaseProjectData() {
        return projectData;
    }

    @Override
    public Path getBuildRoot() {
        return Paths.get(projectData.getRootProjectPath()).resolve(projectData.getRootProjectSubdirectory());
    }

    @Override
    public Path getSourceRoot() {
        return getBuildRoot().resolve(projectData.getSourceSubdirectory());
    }

    @Override
    public String getTargetName() {
        return projectData.getTargetName();
    }
    
    @Override
    public String getTargetIdentifier() {
        return projectData.getIdentifier();
    }

    @Override
    public Integer getExecutionStartSettleTimeMS() {
        if (projectData.getExecution() == null) return null;
        return projectData.getExecution().getExecutionStartSettleTimeMS();
    }

    @Override
    public DeploymentTarget getDeploymentTarget() {
        return projectData.getDeploymentTarget();
    }

    @Override
    public String getDeploymentTargetVersion() {
        return projectData.getDeploymentTargetVersion();
    }

    @Override
    public boolean canExecute() {
        return projectData.getExecution() != null;
    }

    @Override
    public Path getBuildFilePath() {
        return getBuildRoot().resolve(projectData.getBuildFile());
    }

    @Override
    public Map<File, String> getExecutionDeploymentFileMap() {
        if (projectData.getExecution() == null) return null;
        HashMap<File, String> rval = new HashMap<>();

        Map<String, String> dfm = projectData.getExecution().getExecutionFileMap();
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
    public Path getExecutablePath() {
        if (projectData.getExecution() == null) return null;
        return getBuildRoot().resolve(projectData.getExecution().getExecutableFilename());
    }

    @Override
    public String getExecutionPackageIdentifier() {
        if (projectData.getExecution() == null) return null;
        return projectData.getExecution().getExecutionPackageIdentifier();
    }

    @Override
    public String getExecutionMainMethod() {
        if (projectData.getExecution() == null) return null;
        return projectData.getExecution().getExecutionMainMethodClasspath();
    }

    @Override
    public Path getBuildToolPath() {
        return Paths.get(buildToolPath).toAbsolutePath();
    }

    @Override
    public BuildPlatform getBuildPlatform() {
        return projectData.getBuildPlatform();
    }

    @Override
    public boolean canTest() {
        return projectData.getTesting() != null;
    }

    @Override
    public Path getTestResultsPath() {
        if (projectData.getTesting() == null) return null;
        return getBuildRoot().resolve(projectData.getTesting().getTestResultXmlSubdirectory());
    }

    @Override
    public String[] getBuildToolBuildParameters() {
        return projectData.getBuildToolBuildParameters();
    }

    @Override
    public String[] getTestBuildToolParameters() {
        if (projectData.getTesting() == null) return null;
        return projectData.getTesting().getTestBuildToolParameters();
    }

    @Override
    public String[] getPublishBuildToolParameters() {
        if (projectData.getPublishing() == null) return null;
        return projectData.getPublishing().getPublishBuildToolParameters();
    }

    private synchronized Boolean executeCleanAndTest(boolean generateCoverageReport, @Nullable Collection<String> testIdentifiers) throws IOException, InterruptedException {
        if (!canTest()) {
            return null;
        }

        boolean rval;

        LinkedList<String> cmdList = new LinkedList<>();
        cmdList.add(getBuildToolPath().toString());
        cmdList.add("--build-file");
        cmdList.add(getBuildFilePath().toString());
        cmdList.addAll(Arrays.asList(getTestBuildToolParameters()));

        if (testIdentifiers != null) {
            for (String testIdentifier : testIdentifiers) {
                cmdList.add("--tests");
                cmdList.add(testIdentifier);
            }
        }

        if (generateCoverageReport) {
            cmdList.add("jacocoTestReport");
        }

        ImmortalsProcessBuilder pb = new ImmortalsProcessBuilder("BASELINE", "gradle");
        Process p = pb.command(cmdList).start();
        p.waitFor();
        return p.exitValue() == 0;

    }

    public synchronized ClassFileCoverageSet executeCleanTestAndGetCoverage(@Nullable Collection<String> testIdentifiers) throws Exception {
        if (!canTest()) {
            return null;
        }

        Boolean result = executeCleanAndTest(true, testIdentifiers);

        if (result == null || !Files.exists(getTestCoverageReportXmlFileSubpath())) {
            return null;
        }
        return XmlParser.createClassFileCoverageReportsFromFile(getTestCoverageReportXmlFileSubpath().toFile());
    }

    public synchronized TestCaseReportSet executeCleanAndTest(@Nullable Collection<String> testIdentifiers) throws Exception {
        if (!canTest()) {
            return null;
        }

        Boolean result = executeCleanAndTest(false, testIdentifiers);

        if (result == null) {
            return null;
        }
        return XmlParser.getTestResultsFromFlatDirectory(getTestResultsPath().toFile(), getTargetIdentifier(), null);

    }
}
