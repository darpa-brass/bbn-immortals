package mil.darpa.immortals.configuration.sourcecomposer;

import mil.darpa.immortals.core.api.applications.ApplicationDeploymentDetails;
import mil.darpa.immortals.core.api.applications.ApplicationType;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;
import mil.darpa.immortals.das.sourcecomposer.CompositionException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * Defines the application information needed to perform an augmentation
 * <p>
 * Created by awellman@bbn.com on 5/4/17.
 */
public class ApplicationProfile {
    
    public final EnvironmentConfiguration.CompositionTarget compositionTarget;

    public final DeploymentPlatform deploymentPlatform;

    private final String applicationBasePath;

    private final String gradleModificationFile;

    private final LinkedList<ControlPointProfile> controlPoints;

    public Path getSourceApplicationFilepath() {
        return EnvironmentConfiguration.getInstance().immortalsRoot.resolve(applicationBasePath);
    }

    public Path generateTargetApplicationPathValue(String sessionIdentifier) {
        String basePath = EnvironmentConfiguration.getInstance().getSynthesisRootPath(sessionIdentifier).resolve(applicationBasePath).toString();

        return Paths.get((basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath));
    }

    public ControlPointProfile getControlPointByUuid(String uuid) throws CompositionException {

        for (ControlPointProfile cpp : controlPoints) {
            if (cpp.controlPointUuid.equals(uuid)) {
                return cpp;
            }
        }
        throw new CompositionException.ControlPointUuidException(uuid);
    }

    public String getGradleTargetFile() {
        return gradleModificationFile;
    }

    public ApplicationProfile(
            EnvironmentConfiguration.CompositionTarget compositionTarget, DeploymentPlatform deploymentPlatform, String applicationBasePath,
            String gradleModificationFile, LinkedList<ControlPointProfile> controlPoints) {
        this.compositionTarget = compositionTarget;
        this.deploymentPlatform = deploymentPlatform;
        this.applicationBasePath = applicationBasePath;
        this.gradleModificationFile = gradleModificationFile;
        this.controlPoints = controlPoints;
    }
    
    public ApplicationDeploymentDetails getAsApplicationDeploymentDetails(String sessionIdentifier) {
        return new ApplicationDeploymentDetails(
                sessionIdentifier,
                ApplicationType.valueOf(compositionTarget.name()),
                mil.darpa.immortals.core.api.applications.DeploymentPlatform.valueOf(deploymentPlatform.name()),
                getSourceApplicationFilepath().toAbsolutePath().toString(),
                generateTargetApplicationPathValue(sessionIdentifier).toAbsolutePath().toString(),
                gradleModificationFile);
        
    }
}
