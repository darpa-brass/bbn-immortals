package mil.darpa.immortals.das.configuration.applications;

import com.oracle.Copy.TreeCopier;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.das.hacks.MockKnowledgeRepository;
import mil.darpa.immortals.das.hacks.configuration.applications.ApplicationProfile;
import mil.darpa.immortals.das.hacks.configuration.applications.CompositionTarget;
import mil.darpa.immortals.das.sourcecomposer.configuration.DeploymentPlatform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by awellman@bbn.com on 10/30/17.
 */
public class ApplicationDetails {
    
    private final Path augmentationPath;
    private final Path executionPath;
    
    private final String adaptationIdentifier;
    private final ApplicationProfile applicationProfile;
    
    ApplicationDetails(String adaptationIdentifier, ApplicationProfile applicationProfile)
            throws ApplicationDetailsException {
        this.adaptationIdentifier = adaptationIdentifier;
        this.applicationProfile = applicationProfile;
        this.augmentationPath = ImmortalsConfig.getInstance().globals.getAdaptationWorkingDirectory(adaptationIdentifier);
        //Configuration.getInstance().directories.getAugmentationDirectory(this.adaptationIdentifier);
        this.executionPath = ImmortalsConfig.getInstance().globals.getAdaptationWorkingDirectory(adaptationIdentifier);
        this.initFilesystem();
    }
    
    private void initFilesystem() throws ApplicationDetailsException {
        switch (applicationProfile.applicationOrigin) {
            case FILESYSTEM:
                try {
                    Files.createDirectories(augmentationPath);
                    Files.createDirectories(executionPath);
                    
                    Path sourcePath = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve(
                            applicationProfile.applicationBasePath);
                    
                    TreeCopier tc = new TreeCopier(sourcePath,
                            augmentationPath.resolve(applicationProfile.applicationBasePath), false, false);
                    Files.walkFileTree(sourcePath, tc);
                    
                } catch (IOException e) {
                    throw new ApplicationDetailsException(e);
                }
            
            default:
                throw new ApplicationDetailsException("The application origin of type '" + 
                        applicationProfile.applicationOrigin.name() + "' is not currently supported!");
        }
    }
    
    
    public String getApplicationIdentifier() {
        return adaptationIdentifier;
    }
    
    public DeploymentPlatform getDeploymentPlatform() {
        return applicationProfile.deploymentPlatform;
    }
    
    public Path getApplicationProjectPath() throws ApplicationDetailsException {
        return null;
    }
    
    public Path getApplicationDeploymentPath() {
        return null;
    }
    
    public Path getApplicationJavaSourceDirectory() {
        return null;
    }
    
    public Path getApplicationResourceDirectory() {
        return null;
    }
    
    public Path getGradleBuildFilepath() {
        return null;
    }
    
    public static void main(String[] argsv) {
        try {
            MockKnowledgeRepository mkr = MockKnowledgeRepository.getInstance();
            ApplicationProfile ap = mkr.getApplication(CompositionTarget.Client_ATAKLite);
            ApplicationDetails ad = new ApplicationDetails("MyIdentifier", ap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
}
