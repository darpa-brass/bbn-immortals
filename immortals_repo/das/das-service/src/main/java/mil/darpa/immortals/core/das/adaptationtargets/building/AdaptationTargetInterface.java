package mil.darpa.immortals.core.das.adaptationtargets.building;

import mil.darpa.immortals.analysis.adaptationtargets.BuildPlatform;
import mil.darpa.immortals.analysis.adaptationtargets.DeploymentTarget;
import mil.darpa.immortals.analysis.adaptationtargets.ImmortalsGradleProjectData;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 2/22/18.
 */
public interface AdaptationTargetInterface {

    /**
     * @return The raw data from the project this is based on. Note any absolute paths in this object are for the origin and not this project itself!
     */
    ImmortalsGradleProjectData getRawBaseProjectData();

    /**
     * @return Whether or not this adaptation target is publishable
     */
    boolean canPublish();

    /**
     * @return If publishable, the groupId. Null otherwise.
     */
    String getPublishGroupId();

    /**
     * @return If publishable, the artifactId. Null otherwise.
     */
    String getPublishArtifactId();

    /**
     * @return If publishable, the version. Null otherwise.
     */
    String getPublishVersion();

    /**
     * @return If publishable, the full dependency coordinate String in the format 'groupId:artifactId:version'
     */
    String getPublishDependencyCoordinates();

    /**
     * @return The build root of the project
     */
    Path getBuildRoot();

    /**
     * @return the source root of the project that contains package-corresponding source paths
     */
    Path getSourceRoot();

    /**
     * @return The general identifier for the target application. Not unique between instances.
     */
    String getTargetName();

    /**
     * @return If executable, how long the application takes to settle until dependents can utilize it. Null otherwise
     */
    Integer getExecutionStartSettleTimeMS();

    /**
     * @return The target deployment environment
     */
    DeploymentTarget getDeploymentTarget();

    /**
     * @return The target deployment environment version
     */
    String getDeploymentTargetVersion();

    /**
     * @return Whether or not this adaptation target is executable
     */
    boolean canExecute();

    /**
     * @return If executable, the full package path of the main executable class. Null otherwise.
     */
    String getExecutionMainMethod();

    /**
     * @return If executable, the package path for the executable artifact. Null otherwise.
     */
    String getExecutionPackageIdentifier();

    /**
     * @return The mapping of source-path -> target-path files that must be present for deployment. Null if not executable.
     */
    Map<File, String> getExecutionDeploymentFileMap();

    /**
     * @return If executable, the full path of the produced executable artifact. null otherwise.
     */
    Path getExecutablePath();

    /**
     * @return The full path to the build tool
     */
    Path getBuildToolPath();

    /**
     * @return The build tool in use. Currently restricted to "GRADLE".
     */
    BuildPlatform getBuildPlatform();

    /**
     * @return Whether or not this adaptation target can be tested
     */
    boolean canTest();

    /**
     * @return The test result path if this object is testable, null otherwise.
     */
    Path getTestResultsPath();

    /**
     * @return The subparth to the test coverage XML report if this object is testable, null otherwise
     */
    Path getTestCoverageReportXmlFileSubpath();

    /**
     * @return The parameters that must be supplied to the build tool to perform a build.
     */
    String[] getBuildToolBuildParameters();

    /**
     * @return The parameters that must be supplied to the build tool to perform validation, if applicable.
     */
    String[] getTestBuildToolParameters();

    /**
     * @return the parameters that must be supplied to the build tool to perform a validation, if applicable.
     */
    String[] getPublishBuildToolParameters();

    /**
     * @return The path to the file used by the build system to perform a build
     */
    Path getBuildFilePath();
}
