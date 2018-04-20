package mil.darpa.immortals.core.das.knowledgebuilders.building;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mil.darpa.immortals.analysis.adaptationtargets.ImmortalsGradleProjectData;
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
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by awellman@bbn.com on 1/29/18.
 */
public class GradleKnowledgeBuilder implements IKnowledgeBuilder {

    private Logger logger = LoggerFactory.getLogger(GradleKnowledgeBuilder.class);

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, AdaptationTargetBuildBase> adaptationBuildTargetsLong = new HashMap<>();
    private static final Map<String, AdaptationTargetBuildBase> adaptationBuildTargetsShort = new HashMap<>();
    private static final HashMap<String, AdaptationTargetBuildInstance> adaptationBuildInstances = new HashMap<>();

    private static synchronized void loadData() throws IOException {
        if (adaptationBuildTargetsLong.size() == 0) {
            ImmortalsConfig ic = ImmortalsConfig.getInstance();


            // Read in the initially analyzed data
            Path dataFile = ic.extensions.immortalizer.getProducedDataTargetFile();
            JsonObject jsonData = gson.fromJson(new FileReader(dataFile.toFile()), JsonObject.class);

            for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {

                ImmortalsGradleProjectData projectData = gson.fromJson(entry.getValue(), ImmortalsGradleProjectData.class);
                AdaptationTargetBuildBase base = new AdaptationTargetBuildBase(projectData);

                adaptationBuildTargetsShort.put(projectData.getTargetName(), base);
                adaptationBuildTargetsLong.put(projectData.getIdentifier(), base);
            }
        }
    }

    private synchronized void analyzeAdaptationTarget(@Nonnull String identifier, @Nonnull JsonElement data) throws Exception {
        ImmortalsGradleProjectData projectData = gson.fromJson(data, ImmortalsGradleProjectData.class);
        AdaptationTargetBuildBase base = new AdaptationTargetBuildBase(projectData);

        if (base.canTest()) {
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
        } else {
            logger.debug("Project '" + base.getTargetName() + "' has no tests to collect coverage data for.");
        }
    }

    @Override
    public synchronized Model buildKnowledge(Map<String, Object> parameter) throws Exception {

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
        AdaptationTargetBuildInstance buildInstance = adaptationBuildInstances.get(appInstanceIdentifier);

        if (buildInstance == null) {
            AdaptationTargetBuildBase buildBase = adaptationBuildTargetsLong.get(baseApplicationIdentifier);
            if (buildBase == null) {
                buildBase = adaptationBuildTargetsShort.get(baseApplicationIdentifier);
            }
            
            if (buildBase != null) {
                buildInstance = new AdaptationTargetBuildInstance(adaptationIdentifier, buildBase);
                buildInstance.getBuildRoot();
                adaptationBuildInstances.put(appInstanceIdentifier, buildInstance);
            }
        }
        return buildInstance;
    }

    @Nullable
    public static synchronized AdaptationTargetBuildBase getBuildBase(@Nonnull String applicationIdentifier) throws IOException {
        loadData();
        AdaptationTargetBuildBase b = adaptationBuildTargetsLong.get(applicationIdentifier);
        if (b == null) {
            b = adaptationBuildTargetsShort.get(applicationIdentifier);
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
        HashMap<String, Set<String>> rval = new HashMap<> ();

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
