package mil.darpa.immortals.core.das.adaptationtargets.building;

import java.nio.file.Path;

/**
 * Created by awellman@bbn.com on 2/22/18.
 */
public interface AdaptationTargetInterface {

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
    String getOwnDependencyCoordinates();

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
    String getTargetIdentifier();

    /**
     * @return If executable, how long the application takes to settle until dependents can utilize it
     */
    int getSettleTimeMS();

    /**
     * @return The target deployment environment
     */
    DeploymentTarget getDeploymentTarget();

    /**
     * @return The target deployment environment version
     */
    String getDeploymentTargetVersion();

    /**
     * @return If executable, the full package path of the main executable class. Null otherwise.
     */
    String getExecutionMainMethod();

    /**
     * @return If executable, the package path for the executable artifact. Null otherwise.
     */
    String getExecutionPackageIdentifier();

    /**
     * @return The full path to the build tool
     */
    Path getBuildToolPath();

    /**
     * @return The build tool in use. Currently restricted to "GRADLE".
     */
    BuildPlatform getBuildPlatform();

    /**
     * @return The parameters that must be supplied to the build tool to perform a build.
     */
    String getBuildToolBuildParameters();

    /**
     * @return The parameters that must be supplied to the build tool to perform validation, if applicable.
     */
    String getBuildToolValidationParameters();

    /**
     * @return The path to the file used by the build system to perform a build
     */
    Path getBuildFilePath();
}
