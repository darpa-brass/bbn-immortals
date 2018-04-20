package mil.darpa.immortals.core.das;

import mil.darpa.immortals.core.api.TestCaseReport;
import mil.darpa.immortals.core.api.TestCaseReportSet;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetailsList;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;
import mil.darpa.immortals.core.das.adaptationmodules.hddrass.Hacks;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.core.das.sparql.adaptationtargets.DetermineAllAnnotatedTestFunctionality;
import mil.darpa.immortals.core.das.sparql.adaptationtargets.DetermineTargetDependencyCoordinates;
import mil.darpa.immortals.core.das.sparql.deploymentmodel.DetermineTargets;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 3/21/18.
 */
public class ValidationManager {

    private Logger logger = LoggerFactory.getLogger(ValidationManager.class);

    private final DasAdaptationContext dac;

    public ValidationManager(DasAdaptationContext dac) {
        this.dac = dac;
    }

    public synchronized TestCaseReportSet executeValidation(boolean reportStatus) throws Exception {

        // First produce a list of pending tests and report it to the server
        Map<String, Set<String>> allTests = GradleKnowledgeBuilder.getAllTargetTests();
        TestCaseReportSet tcrsInitial = new TestCaseReportSet();
        for (String artifactIdentifier : allTests.keySet()) {
            Set<String> tests = allTests.get(artifactIdentifier);
            for (String testIdentifier : tests) {
                tcrsInitial.add(new TestCaseReport(
                        artifactIdentifier,
                        testIdentifier,
                        0,
                        null,
                        null
                ));
            }
        }

        TestDetailsList initialTdl = TestDetailsList.fromTestCaseReportSet(dac.getAdaptationIdentifer(), tcrsInitial);
        if (reportStatus) {
            dac.submitValidationStatus(initialTdl.producePendingList());
        }

        // TODO: Not this!
        Thread.sleep(300);

        if (reportStatus) {
            dac.submitValidationStatus(initialTdl.produceRunningList());
        }

        // TODO: Not this!
        Thread.sleep(300);

//        Set<String> testsToExecute = cfcs.stream().map(t -> t.getIdentifier()).collect(Collectors.toSet());


        TestCaseReportSet testCaseReports = new TestCaseReportSet();

        // TODO: Currently getting from resourcecontainmentnode contained resources. Should it be the top level resources?
        // TODO: Do away with the baseline test validator. Should be checking Test annotations directly for completeness

        // First, get all the functionality bound to tests
        Map<String, Map<String, Set<String>>> functionality = DetermineAllAnnotatedTestFunctionality.select(dac);

        Set<String> allDependencyCoordinates = new HashSet<>();

        // For each top level application
        for (String target : DetermineTargets.select(dac)) {
            // Determine its proper identifier
            // TODO: Align identifiers better
            String identifier = target.substring(target.indexOf(":")+1, target.lastIndexOf(":"));

            // Add all its dependencies coordinates
            allDependencyCoordinates.addAll(DetermineTargetDependencyCoordinates.select(dac, identifier));

            // Get a build instance for the top level application
            AdaptationTargetBuildInstance atbi = GradleKnowledgeBuilder.getBuildInstance(identifier, dac.getAdaptationIdentifer());

            // And execute all tests, adding them to the set of test reports
            TestCaseReportSet tcrs = atbi.executeCleanAndTest(functionality.getOrDefault(identifier, null));
            testCaseReports.addAll(tcrs);
            if (tcrs.size() > 0) {
                logger.info("Performed initial validation on '" + identifier + "' with test results of:\n" + tcrs.getResultChart());
            } else {
                logger.debug("Performed initial validation on '" + identifier + "' with no test results.\n");
            }
        }

        // Then, for all dependencies in use for all applications, do the same if possible, minus collection of transitive dependencies
        for (String identifier : allDependencyCoordinates) {

            // Get a build instance
            AdaptationTargetBuildInstance atbi = GradleKnowledgeBuilder.getBuildInstance(identifier, dac.getAdaptationIdentifer());

            if (atbi != null && atbi.canTest()) {
                // If one exists (Indicating we have the source, execute all tests and add it to the test reports
                TestCaseReportSet tcrs = atbi.executeCleanAndTest(functionality.get(identifier));
                testCaseReports.addAll(tcrs);
                logger.info("Performed initial validation on '" + identifier + "' with test results of:\n" + tcrs.getResultChart());
            } else {
                logger.debug("Skipping tests for '" + identifier + "'.");
            }
        }
        if (reportStatus) {
            dac.submitValidationStatus(TestDetailsList.fromTestCaseReportSet(dac.getAdaptationIdentifer(), testCaseReports));
        }
        return testCaseReports;
    }
}
