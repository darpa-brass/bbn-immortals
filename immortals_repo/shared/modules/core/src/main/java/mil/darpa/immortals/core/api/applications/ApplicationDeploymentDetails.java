package mil.darpa.immortals.core.api.applications;

/**
 * Created by awellman@bbn.com on 9/6/17.
 */
public class ApplicationDeploymentDetails {
    
    public final String sessionIdentifier;

    public final ApplicationType compositionTarget;

    public final DeploymentPlatform deploymentPlatform;

    public final String applicationBasePath;

    public final String applicationTargetPath;

    public final String gradleModificationFile;

    public ApplicationDeploymentDetails(
            String sessionIdentifier, ApplicationType compositionTarget, DeploymentPlatform deploymentPlatform,
            String applicationBasePath, String applicationTargetPath, String gradleModificationFile) {
        this.sessionIdentifier = sessionIdentifier;
        this.compositionTarget = compositionTarget;
        this.deploymentPlatform = deploymentPlatform;
        this.applicationBasePath = applicationBasePath;
        this.applicationTargetPath = applicationTargetPath;
        this.gradleModificationFile = gradleModificationFile;
    }
}
