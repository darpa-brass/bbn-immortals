package mil.darpa.immortals.core.das.knowledgebuilders.building;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mil.darpa.immortals.analysis.adaptationtargets.DeploymentTarget;
import mil.darpa.immortals.analysis.adaptationtargets.ImmortalsGradleProjectData;
import mil.darpa.immortals.config.GlobalsConfig;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.TestCaseReport;
import mil.darpa.immortals.core.api.TestCaseReportSet;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildBase;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.adaptationtargets.testing.ClassFileCoverageSet;
import mil.darpa.immortals.core.das.adaptationtargets.testing.XmlParser;
import mil.darpa.immortals.core.das.knowledgebuilders.IKnowledgeBuilder;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by awellman@bbn.com on 1/29/18.
 */
public class GradleKnowledgeBuilder implements IKnowledgeBuilder {

    private Logger logger = LoggerFactory.getLogger(GradleKnowledgeBuilder.class);

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, AdaptationTargetBuildBase> adaptationBuildTargetsLong = new HashMap<>();
    private static final Map<String, String> adaptationBuildTargetsShort = new HashMap<>();
    private static final Map<String, String> adaptationBuildTargetsCoordinates = new HashMap<>();

    private static final HashMap<String, AdaptationTargetBuildInstance> adaptationBuildInstancesLong = new HashMap<>();
    private static final Map<String, String> adaptationBuildInstancesShort = new HashMap<>();
    private static final Map<String, String> adaptationBuildInstancesCoordinates = new HashMap<>();

    private static synchronized void performGradleAnalysis() {
        String[] targets = ImmortalsConfig.getInstance().getTargetApplicationUris();
        Process p = null;

        for (String str : targets) {
            Path projectPath;

            if (Files.exists(Paths.get(str))) {
                projectPath = Paths.get(str);
            } else {
                projectPath = GlobalsConfig.staticImmortalsRoot.resolve(str);
            }

            Path buildFilePath = projectPath.resolve("build.gradle");
            if (!Files.exists(projectPath) || !Files.exists(buildFilePath)) {
                throw new RuntimeException("No build.gradle found in the path '" + projectPath.toString() + "'!");

            } else {
                try {
                    ProcessBuilder pb = new ProcessBuilder();
                    String[] cmd = {
                            ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("gradlew").toString(),
                            "--build-file",
                            buildFilePath.toString(),
                            "clean",
                            "build",
                            "immortalize"
                    };
                    pb.command(cmd);
                    pb.directory(projectPath.toFile());
                    pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

                    String override_file_value = System.getenv("IMMORTALS_OVERRIDE_FILE");
                    if (override_file_value != null && !override_file_value.equals("")) {
                        pb.environment().put("IMMORTALS_OVERRIDE_FILE", override_file_value);
                    }

                    p = pb.start();
                    p.waitFor(300000, TimeUnit.MILLISECONDS);

                    if (p.exitValue() != 0) {
                        throw new RuntimeException("Immortalization did not behave as expected for '" + projectPath.toString() + "'!");
                    }
                } catch (IOException | InterruptedException e) {
                    if (p != null) {
                        p.destroyForcibly();
                    }
                    throw new RuntimeException("Immortalization did not behave as expected for '" + projectPath.toString() + "'!");
                }
            }
        }
    }

    private static synchronized void loadData() throws IOException {
        if (adaptationBuildTargetsLong.size() == 0) {
            ImmortalsConfig ic = ImmortalsConfig.getInstance();


            // Read in the initially analyzed data
            Path dataFile = ic.extensions.immortalizer.getProducedDataTargetFile();
            JsonObject jsonData = gson.fromJson(new FileReader(dataFile.toFile()), JsonObject.class);

            for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {

                ImmortalsGradleProjectData projectData = gson.fromJson(entry.getValue(), ImmortalsGradleProjectData.class);
                AdaptationTargetBuildBase base = new AdaptationTargetBuildBase(projectData);
                adaptationBuildTargetsLong.put(projectData.getIdentifier(), base);
                adaptationBuildTargetsShort.put(projectData.getTargetName(), projectData.getIdentifier());

                if (projectData.getPublishing() != null) {
                    adaptationBuildTargetsCoordinates.put(projectData.getPublishing().getPublishCoordinates(), projectData.getIdentifier());
                }
            }
        }
    }

    private synchronized void analyzeAdaptationTarget(@Nonnull String identifier, @Nonnull JsonElement data) throws Exception {
        ImmortalsGradleProjectData projectData = gson.fromJson(data, ImmortalsGradleProjectData.class);
        AdaptationTargetBuildBase base = new AdaptationTargetBuildBase(projectData);

        if (base.canTest()) {
            // TODO: This probably shouldn't simply be disabled for android applications...
            if (base.getDeploymentTarget() == DeploymentTarget.ANDROID) {
                logger.info("Collection of test coverage data for android project '" + base.getTargetName() + "' not yet supported.");

            } else {

                logger.info("Collecting test coverage data for project '" + base.getTargetName() + "'.");
                HashMap<String, ClassFileCoverageSet> artifactTestClasses = new HashMap<>();
                Set<TestCaseReportSet> artifactTestReports = new HashSet<>();

                TestCaseReportSet allResults = base.executeCleanAndTest(null);
                TestCaseReportSet tcrs = XmlParser.getTestResultsFromFlatDirectory(base.getTestResultsPath().toFile(), base.getTargetName(), null);

                for (TestCaseReport report : allResults) {
                    List<String> tests = Arrays.asList(report.getTestCaseIdentifier());
                    ClassFileCoverageSet coverage = base.executeCleanTestAndGetCoverage(tests);
                    artifactTestClasses.put(report.getTestCaseIdentifier(), coverage);
                }

                JsonElement je = gson.toJsonTree(artifactTestClasses);
                data.getAsJsonObject().add("baseTestClassFileCoverage", je);

                je = gson.toJsonTree(tcrs);
                data.getAsJsonObject().add("baseTestCaseReports", je);

                logger.info("Collection of test coverage data for project '" + base.getTargetName() + "' done.");
            }
        } else {
            logger.debug("Project '" + base.getTargetName() + "' has no tests to collect coverage data for.");
        }
    }

    @Override
    public synchronized Model buildKnowledge(Map<String, Object> parameter) throws Exception {

        performGradleAnalysis();

        if (ImmortalsConfig.getInstance().extensions.immortalizer.isPerformTestCoverageAnalysis()) {
            logger.info("Executing test coverage.");

            ImmortalsConfig ic = ImmortalsConfig.getInstance();

            // Read in the initially analyzed data
            Path dataFile = ic.extensions.immortalizer.getProducedDataTargetFile();
            JsonObject jsonData = gson.fromJson(new FileReader(dataFile.toFile()), JsonObject.class);

            for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
                // Update the data in memory
                analyzeAdaptationTarget(entry.getKey(), entry.getValue());
            }
            // And then update the data on disk
            FileWriter fw = new FileWriter(dataFile.toFile());
            gson.toJson(jsonData, fw);
            fw.flush();
            fw.close();
        } else {
            logger.debug("Skipping execution of test coverage.");

        }
        return null;
    }

    public static synchronized AdaptationTargetBuildInstance getBuildInstance(@Nonnull String baseApplicationIdentifier,
                                                                              @Nonnull String adaptationIdentifier) throws IOException {
        loadData();

        String appInstanceIdentifier = baseApplicationIdentifier + "-" + adaptationIdentifier;
        AdaptationTargetBuildInstance buildInstance = adaptationBuildInstancesLong.get(appInstanceIdentifier);

        if (buildInstance == null) {
            String key = adaptationBuildInstancesShort.get(adaptationIdentifier);
            if (key == null) {
                key = adaptationBuildInstancesCoordinates.get(adaptationIdentifier);
            }

            buildInstance = adaptationBuildInstancesLong.get(key);
        }

        if (buildInstance == null) {
            AdaptationTargetBuildBase buildBase = getBuildBase(baseApplicationIdentifier);

            if (buildBase != null) {
                buildInstance = new AdaptationTargetBuildInstance(adaptationIdentifier, buildBase);
                buildInstance.getBuildRoot();
                adaptationBuildInstancesLong.put(appInstanceIdentifier, buildInstance);
            }
        }
        return buildInstance;
    }

    @Nullable
    public static synchronized AdaptationTargetBuildBase getBuildBase(@Nonnull String applicationIdentifier) throws IOException {
        loadData();
        AdaptationTargetBuildBase b = adaptationBuildTargetsLong.get(applicationIdentifier);
        if (b == null) {
            String key = adaptationBuildTargetsShort.get(applicationIdentifier);
            if (key == null) {
                key = adaptationBuildTargetsCoordinates.get(applicationIdentifier);
            }
            b = adaptationBuildTargetsLong.get(key);
        }
        return b;
    }

//    public static synchronized HashMap<String, Set<String>> getAllTargetTests() throws IOException {
//        loadData();
//        HashMap<String, Set<String>> rval = new HashMap<>();
//
//        for (Map.Entry<String, AdaptationTargetBuildBase> entry : adaptationBuildTargets.entrySet()) {
//            ImmortalsGradleProjectData rawData = entry.getValue().getRawBaseProjectData();
//            if (rawData.getBaseTestClassFileCoverage() != null && rawData.getBaseTestClassFileCoverage().size() > 0) {
//                rval.put(entry.getKey(), rawData.getBaseTestClassFileCoverage().keySet());
//            }
//        }
//        return rval;
//    }

    public static synchronized HashMap<String, Set<String>> getAllTargetTests() throws IOException {
        loadData();
        HashMap<String, Set<String>> rval = new HashMap<>();

        for (Map.Entry<String, AdaptationTargetBuildBase> entry : adaptationBuildTargetsLong.entrySet()) {
            ImmortalsGradleProjectData rawData = entry.getValue().getRawBaseProjectData();
            Set<TestCaseReport> tcrs = rawData.getBaseTestReports();
            if (tcrs != null && tcrs.size() > 0) {
                rval.put(entry.getKey(), tcrs.stream().map(TestCaseReport::getTestCaseIdentifier).collect(Collectors.toSet()));
            }
        }
        return rval;
    }

    public static void main(String[] args) {
        try {
            GradleKnowledgeBuilder gkb = new GradleKnowledgeBuilder();
            gkb.buildKnowledge(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
