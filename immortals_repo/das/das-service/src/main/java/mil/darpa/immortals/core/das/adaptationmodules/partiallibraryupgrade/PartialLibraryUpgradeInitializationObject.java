package mil.darpa.immortals.core.das.adaptationmodules.partiallibraryupgrade;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 2/28/18.
 */
public class PartialLibraryUpgradeInitializationObject {
    
    private class Artifact {
        private final String jarFilepath;
        
        public Artifact(@Nonnull String jarFilepath) {
            this.jarFilepath = jarFilepath;
        }
    }
    
    private final Artifact currentlyUsedLibrary;
    
    private final Artifact targetUpgradeLibrary;
    
    private final LinkedList<Artifact> targetApplications;
    
    public PartialLibraryUpgradeInitializationObject() {
        this.currentlyUsedLibrary = new Artifact("/absolute/path/to/library/currently/used/by/applications.jar");
        this.targetUpgradeLibrary = new Artifact("/absolute/path/to/library/containing/security/fix.jar");
        this.targetApplications = new LinkedList<>(Arrays.asList(
                new Artifact("/absolute/path/to/application/using/currently/used/library")
        ));
    }
    
    
    

//    private final String buildToolIdentifier;
//    private final String buildToolPath;
//    private final String buildToolBuildParameter;
//    private final String buildToolValidationParameters;
//    private final String applicationPath;
//    private final String sourceSubpath;
//    private final String buildFilePath;
//    private final String testResultPath;
//    private final String testFileRegex;
//    private final String BUILD_SUCCESS_STRING;
//    private final String RUN_SUCCESS_STRING;
//    private final String PACKAGE_NAME;
//    private final String TEST_RESULT_PATH;
//    private final String TEST_FILE_REGEX;
//    private final String INTERMEDIATE_TEST_RESULT_OUTPUTPATH;
//    private final String INTERMEDIATE_COMPILE_OUTPUT_PATH;

//    public HddRassInitializationObject(AdaptationTargetBuildInstance buildInstance, List<String> requiredValidators) {
//        Path buildRoot = buildInstance.getBuildRoot();
//        this.requiredValidators = new LinkedList<>(requiredValidators);
//        this.buildTool = buildInstance.getBuildPlatform().command;
//        this.buildToolPath = buildInstance.getBuildToolPath().toString();
//        this.buildToolBuildParameter = buildInstance.getBuildToolBuildParameters();
//        this.buildToolValidationParameters = buildInstance.getBuildToolValidationParameters();
//        this.applicationPath = buildRoot.toString() + "/";
//        this.sourceSubpath = buildInstance.getProjectSourceSubdirectory();
//        this.buildFilePath = buildRoot.relativize(buildInstance.getBuildFilePath()).toString();
//        this.testResultPath = buildInstance.getTestResultsPath().toString() + "/";
//        this.testFileRegex = "";
//        this.BUILD_SUCCESS_STRING = "BUILD SUCCESS";
//        this.RUN_SUCCESS_STRING = "tests=\"" + Integer.toString(requiredValidators.size()) + "\" skipped=\"0\" failures=\"0\" errors=\"0\"";
//        // TODO: THIS CANNOT BE HARD-CODED!
//        this.PACKAGE_NAME = "com.bbn.marti.Tests";
//        TEST_RESULT_PATH = this.testResultPath;
//        // TODO: Can this just be .*xml.*?
//        TEST_FILE_REGEX = ".*Tests.*xml.*";
//        INTERMEDIATE_TEST_RESULT_OUTPUTPATH = ImmortalsConfig.getInstance().extensions.hddrass.
//                getExecutionWorkingDirectory(buildInstance.getAdaptationIdentifier()).toAbsolutePath().toString() + "/";
//        INTERMEDIATE_COMPILE_OUTPUT_PATH = Paths.get(INTERMEDIATE_TEST_RESULT_OUTPUTPATH).resolve("tempAntCompile.txt").toAbsolutePath().toString();
//    } 
}
