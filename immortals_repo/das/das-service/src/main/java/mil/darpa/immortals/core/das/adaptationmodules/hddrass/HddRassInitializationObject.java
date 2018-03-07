package mil.darpa.immortals.core.das.adaptationmodules.hddrass;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 1/29/18.
 */
public class HddRassInitializationObject {

    private final LinkedList<String> requiredValidators;
    private final String buildTool;
    private final String buildToolPath;
    private final String buildToolBuildParameter;
    private final String buildToolValidationParameters;
    private final String applicationPath;
    private final String sourceSubpath;
    private final String buildFilePath;
    private final String testResultPath;
    private final String testFileRegex;
    private final String BUILD_SUCCESS_STRING;
    private final String RUN_SUCCESS_STRING;
    private final String PACKAGE_NAME;
    private final String TEST_RESULT_PATH;
    private final String TEST_FILE_REGEX;
    private final String INTERMEDIATE_TEST_RESULT_OUTPUTPATH;
    private final String INTERMEDIATE_COMPILE_OUTPUT_PATH;

    public HddRassInitializationObject(AdaptationTargetBuildInstance buildInstance, List<String> requiredValidators) {
        Path buildRoot = buildInstance.getBuildRoot();
        this.requiredValidators = new LinkedList<>(requiredValidators);
        this.buildTool = buildInstance.getBuildPlatform().command;
        this.buildToolPath = buildInstance.getBuildToolPath().toString();
        this.buildToolBuildParameter = buildInstance.getBuildToolBuildParameters();
        this.buildToolValidationParameters = buildInstance.getBuildToolValidationParameters();
        this.applicationPath = buildRoot.toString() + "/";
        this.sourceSubpath = buildInstance.getProjectSourceSubdirectory();
        this.buildFilePath = buildRoot.relativize(buildInstance.getBuildFilePath()).toString();
        this.testResultPath = buildInstance.getTestResultsPath().toString() + "/";
        this.testFileRegex = "";
        this.BUILD_SUCCESS_STRING = "BUILD SUCCESS";
        this.RUN_SUCCESS_STRING = "tests=\"" + Integer.toString(requiredValidators.size()) + "\" skipped=\"0\" failures=\"0\" errors=\"0\"";
        // TODO: THIS CANNOT BE HARD-CODED!
        this.PACKAGE_NAME = "com.bbn.marti.Tests";
        TEST_RESULT_PATH = this.testResultPath;
        // TODO: Can this just be .*xml.*?
        TEST_FILE_REGEX = ".*Tests.*xml.*";
        INTERMEDIATE_TEST_RESULT_OUTPUTPATH = ImmortalsConfig.getInstance().extensions.hddrass.
                getExecutionWorkingDirectory(buildInstance.getAdaptationIdentifier()).toAbsolutePath().toString() + "/";
        INTERMEDIATE_COMPILE_OUTPUT_PATH = Paths.get(INTERMEDIATE_TEST_RESULT_OUTPUTPATH).resolve("tempAntCompile.txt").toAbsolutePath().toString();


    }
}
