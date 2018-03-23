package mil.darpa.immortals.core.das.adaptationtargets.building;

import mil.darpa.immortals.config.ImmortalsConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 1/25/18.
 */
public class AdaptationTargetBuildBase implements AdaptationTargetInterface {

    private String targetIdentifier;
    private int settleTimeMS;
    private DeploymentTarget deploymentTarget;
    private String deploymentTargetVersion;
    private String repoLocation;
    private String projectRepoPath;
    private String buildFile;
    private String executableFile;
    private Map<String, String> deploymentFileMap;
    private String sourceSubdirectory;
    private String executionPackageIdentifier;
    private String executionMainMethod;
    private String buildToolPath;
    private BuildPlatform buildPlatform;
    private String[] buildToolBuildParameters;
    private String[] buildToolValidationParameters;
    private String buildToolTestResultsSubdirectory;
    private String[] buildToolPublishParameters;
    private String publishGroupId;
    private String publishArtifactId;
    private String publishVersion;

    public AdaptationTargetBuildBase() {
    }

    public AdaptationTargetBuildBase(String targetIdentifier, int settleTimeMS, DeploymentTarget deploymentTarget,
                                     String deploymentTargetVersion, String repoLocation, String projectRepoPath,
                                     String executableFile, String buildFile, Map<String, String> deploymentFileMap,
                                     String sourceSubdirectory, String executionPackageIdentifier, String executionMainMethod,
                                     String buildToolPath, BuildPlatform buildPlatform, String[] buildToolBuildParameters,
                                     String[] buildToolValidationParameters, String buildToolTestResultsSubdirectory,
                                     String[] buildToolPublishParameters, String publishGroupId, String publishArtifactId,
                                     String publishVersion) {
        this.targetIdentifier = targetIdentifier;
        this.settleTimeMS = settleTimeMS;
        this.deploymentTarget = deploymentTarget;
        this.deploymentTargetVersion = deploymentTargetVersion;
        this.repoLocation = repoLocation;
        this.projectRepoPath = projectRepoPath;
        this.buildFile = buildFile;
        this.executableFile = executableFile;
        this.deploymentFileMap = deploymentFileMap;
        this.sourceSubdirectory = sourceSubdirectory;
        this.executionPackageIdentifier = executionPackageIdentifier;
        this.executionMainMethod = executionMainMethod;
        this.buildToolPath = buildToolPath;
        this.buildPlatform = buildPlatform;
        this.buildToolBuildParameters = buildToolBuildParameters;
        this.buildToolValidationParameters = buildToolValidationParameters;
        this.buildToolTestResultsSubdirectory = buildToolTestResultsSubdirectory;
        this.buildToolPublishParameters = buildToolPublishParameters;
        this.publishGroupId = publishGroupId;
        this.publishArtifactId = publishArtifactId;
        this.publishVersion = publishVersion;
    }

    public String getPublishGroupId() {
        return publishGroupId;
    }

    public String getPublishArtifactId() {
        return publishArtifactId;
    }

    public String getPublishVersion() {
        return publishVersion;
    }

    public String getOwnDependencyCoordinates() {
        return publishGroupId + ":" + publishArtifactId + ":" + publishVersion;
    }

    @Override
    public Path getBuildRoot() {
        return ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve(projectRepoPath);
    }

    @Override
    public Path getSourceRoot() {
        return getBuildRoot().resolve(sourceSubdirectory);
    }

    @Override
    public String getTargetIdentifier() {
        return targetIdentifier;
    }

    @Override
    public int getSettleTimeMS() {
        return settleTimeMS;
    }

    @Override
    public DeploymentTarget getDeploymentTarget() {
        return deploymentTarget;
    }

    @Override
    public String getDeploymentTargetVersion() {
        return deploymentTargetVersion;
    }

    String getRepoLocation() {
        return repoLocation;
    }

    String getProjectRepoPath() {
        return projectRepoPath;
    }

    String getExecutableFile() {
        return executableFile;
    }

    String getBuildFile() {
        return buildFile;
    }

    @Override
    public Path getBuildFilePath() {
        return getBuildRoot().resolve(buildFile);
    }

    Map<String, String> getDeploymentFileMap() {
        return new HashMap<>(deploymentFileMap);
    }

    String getSourceSubdirectory() {
        return sourceSubdirectory;
    }

    @Override
    public String getExecutionPackageIdentifier() {
        return executionPackageIdentifier;
    }

    @Override
    public String getExecutionMainMethod() {
        return executionMainMethod;
    }

    @Override
    public Path getBuildToolPath() {
        return Paths.get(buildToolPath).toAbsolutePath();
    }

    @Override
    public BuildPlatform getBuildPlatform() {
        return buildPlatform;
    }

    @Override
    public String[] getBuildToolBuildParameters() {
        return buildToolBuildParameters;
    }

    @Override
    public String[] getBuildToolValidationParameters() {
        return buildToolValidationParameters;
    }

    String getBuildToolTestResultsSubdirectory() {
        return buildToolTestResultsSubdirectory;
    }
    
    @Override
    public String[] getBuildToolPublishParameters() {
        return buildToolPublishParameters;
    }
}
