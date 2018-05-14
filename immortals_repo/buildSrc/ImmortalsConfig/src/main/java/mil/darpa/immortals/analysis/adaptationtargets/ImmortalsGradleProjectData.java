package mil.darpa.immortals.analysis.adaptationtargets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.TestCaseReport;
import mil.darpa.immortals.core.das.adaptationtargets.testing.ClassFileCoverageSet;

import javax.annotation.Nullable;
import java.io.FileReader;
import java.util.*;

public class ImmortalsGradleProjectData {

    private static transient Map<String, ImmortalsGradleProjectData> immortalsGradleProjectData;

    private final String targetName;
    private final String targetGroup;
    private final String targetVersion;
    private final DeploymentTarget deploymentTarget;
    private final String deploymentTargetVersion;
    private final String rootProjectPath;
    private final String rootProjectSubdirectory;
    private final String buildFile;
    private final String sourceSubdirectory;
    private final BuildPlatform buildPlatform;
    private final String[] buildToolBuildParameters;
    private final ImmortalsGradleTestData testing;
    private final ImmortalsGradlePublishData publishing;
    private final ImmortalsGradleExecutionData execution;
    private final HashMap<String, ClassFileCoverageSet> baseTestClassFileCoverage;
    private final HashSet<TestCaseReport> baseTestCaseReports;

    public ImmortalsGradleProjectData(String targetName,
                                      String targetGroup,
                                      String targetVersion,
                                      DeploymentTarget deploymentTarget,
                                      String deploymentTargetVersion,
                                      String rootProjectPath,
                                      String rootProjectSubdirectory,
                                      String buildFile,
                                      String sourceSubdirectory,
                                      BuildPlatform buildPlatform,
                                      String[] buildToolBuildParameters,
                                      ImmortalsGradleTestData testing,
                                      ImmortalsGradlePublishData publishing,
                                      ImmortalsGradleExecutionData executionData,
                                      HashMap<String, ClassFileCoverageSet> baseTestClassFileCoverage,
                                      Collection<TestCaseReport> baseTestCaseReports) {
        this.targetName = targetName;
        this.targetGroup = targetGroup;
        this.targetVersion = targetVersion;
        this.deploymentTarget = deploymentTarget;
        this.deploymentTargetVersion = deploymentTargetVersion;
        this.rootProjectPath = rootProjectPath;
        this.rootProjectSubdirectory = rootProjectSubdirectory;
        this.buildFile = buildFile;
        this.sourceSubdirectory = sourceSubdirectory;
        this.buildPlatform = buildPlatform;
        this.buildToolBuildParameters = buildToolBuildParameters;
        this.testing = testing;
        this.publishing = publishing;
        this.baseTestClassFileCoverage = baseTestClassFileCoverage;
        if (baseTestCaseReports == null) {
            this.baseTestCaseReports = new HashSet<>();
        } else {
            this.baseTestCaseReports = new HashSet<>(baseTestCaseReports);
        }


        String identifier = getIdentifier();

        HashMap<String, String> deploymentFileMap = new HashMap<>();

        switch (identifier) {
            case "ATAKLite":
                deploymentFileMap.put(ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve(
                        "harness/pymmortals/resources/applications/ataklite_baseline/sdcard/ataklite/ATAKLite-Config.json").toString(),
                        "/sdcard/ataklite/ATAKLite-Config.json");
                deploymentFileMap.put(ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve(
                        "harness/pymmortals/resources/applications/ataklite_baseline/sdcard/ataklite/sample_image.jpg").toString(),
                        "/sdcard/ataklite/sample_image.jpg");
                deploymentFileMap.put(ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve(
                        "harness/pymmortals/resources/applications/ataklite_baseline/sdcard/ataklite/env.json").toString(),
                        "/sdcard/ataklite/env.json");

                this.execution = new ImmortalsGradleExecutionData(
                        executionData.getExecutionStartSettleTimeMS(),
                        executionData.getExecutableFilename(),
                        deploymentFileMap,
                        executionData.getExecutionPackageIdentifier(),
                        executionData.getExecutionMainMethodClasspath()
                );
                break;

            case "Marti":
                deploymentFileMap.put("Marti-Config.json", "Marti-Config.json");

                this.execution = new ImmortalsGradleExecutionData(
                        executionData.getExecutionStartSettleTimeMS(),
                        executionData.getExecutableFilename(),
                        deploymentFileMap,
                        executionData.getExecutionPackageIdentifier(),
                        executionData.getExecutionMainMethodClasspath()
                );

                break;

            default:
                this.execution = executionData;

        }
    }

    public String getIdentifier() {
        return getTargetGroup() + ":" + getTargetName() + ":" + getTargetVersion();
    }

    public static Map<String, ImmortalsGradleProjectData> getDataFromFile() throws Exception {
        if (immortalsGradleProjectData == null) {
            Gson gson = new Gson();
            JsonArray ja = gson.fromJson(new FileReader(
                            ImmortalsConfig.getInstance().extensions.immortalizer.getProducedDataTargetFile().toFile()),
                    JsonArray.class);

            immortalsGradleProjectData = new HashMap<>();

            for (JsonElement je : ja) {
                ImmortalsGradleProjectData data = gson.fromJson(je, ImmortalsGradleProjectData.class);
                immortalsGradleProjectData.put(data.getIdentifier(), data);
            }
        }
        return new HashMap<>(immortalsGradleProjectData);
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTargetGroup() {
        return targetGroup;
    }

    public String getTargetVersion() {
        return targetVersion;
    }

    public DeploymentTarget getDeploymentTarget() {
        return deploymentTarget;
    }

    public String getDeploymentTargetVersion() {
        return deploymentTargetVersion;
    }

    public String getRootProjectPath() {
        return rootProjectPath;
    }

    public String getRootProjectSubdirectory() {
        return rootProjectSubdirectory;
    }

    public String getBuildFile() {
        return buildFile;
    }

    public String getSourceSubdirectory() {
        return sourceSubdirectory;
    }

    public BuildPlatform getBuildPlatform() {
        return buildPlatform;
    }

    public String[] getBuildToolBuildParameters() {
        return buildToolBuildParameters.clone();
    }

    @Nullable
    public ImmortalsGradleTestData getTesting() {
        return testing;
    }

    @Nullable
    public ImmortalsGradlePublishData getPublishing() {
        return publishing;
    }

    @Nullable
    public ImmortalsGradleExecutionData getExecution() {
        return execution;
    }

    public Set<TestCaseReport> getBaseTestReports() {
        return baseTestCaseReports;
    }

    public HashMap<String, ClassFileCoverageSet> getBaseTestClassFileCoverage() {
        if (baseTestClassFileCoverage == null) return null;

        HashMap<String, ClassFileCoverageSet> rval = new HashMap<>();

        for (String key : baseTestClassFileCoverage.keySet()) {
            rval.put(key, new ClassFileCoverageSet(baseTestClassFileCoverage.get(key)));
        }
        return rval;
    }
}
