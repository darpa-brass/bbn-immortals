package mil.darpa.immortals.das.hacks.configuration.applications;

import mil.darpa.immortals.core.api.applications.ApplicationDeploymentDetails;
import mil.darpa.immortals.core.api.applications.ApplicationType;
import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;
import mil.darpa.immortals.das.sourcecomposer.CompositionException;
import mil.darpa.immortals.das.sourcecomposer.configuration.DeploymentPlatform;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * Defines the application information needed to perform an augmentation
 * <p>
 * Created by awellman@bbn.com on 5/4/17.
 */
@Deprecated
public class ApplicationProfile {
    
    public final CompositionTarget compositionTarget;

    public final DeploymentPlatform deploymentPlatform;


    public final ApplicationOrigin applicationOrigin;

    public final String applicationBasePath;

    public final String gradleModificationFile;

    private final LinkedList<ControlPointProfile> controlPoints;

    @Deprecated
    public Path getSourceApplicationFilepath() {
        return EnvironmentConfiguration.getInstance().immortalsRoot.resolve(applicationBasePath);
    }

    @Deprecated
    public Path generateTargetApplicationPathValue(String sessionIdentifier) {
        String basePath = EnvironmentConfiguration.getInstance().getSynthesisRootPath(sessionIdentifier).resolve(applicationBasePath).toString();

        return Paths.get((basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath));
    }

    @Deprecated
    public ControlPointProfile getControlPointByUuid(String uuid) throws CompositionException {

        for (ControlPointProfile cpp : controlPoints) {
            if (cpp.controlPointUuid.equals(uuid)) {
                return cpp;
            }
        }
        throw new CompositionException.ControlPointUuidException(uuid);
    }

    @Deprecated
    public String getGradleTargetFile() {
        return gradleModificationFile;
    }

    @Deprecated
    public ApplicationProfile(
            CompositionTarget compositionTarget, DeploymentPlatform deploymentPlatform, ApplicationOrigin applicationOrigin,
            String applicationBasePath,
            String gradleModificationFile, LinkedList<ControlPointProfile> controlPoints) {
        this.compositionTarget = compositionTarget;
        this.deploymentPlatform = deploymentPlatform;
        this.applicationOrigin = applicationOrigin;
        this.applicationBasePath = applicationBasePath;
        this.gradleModificationFile = gradleModificationFile;
        this.controlPoints = controlPoints;
    }

    @Deprecated
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
