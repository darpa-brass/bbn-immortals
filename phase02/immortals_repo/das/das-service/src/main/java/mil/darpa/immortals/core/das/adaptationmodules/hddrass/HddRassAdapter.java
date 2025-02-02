package mil.darpa.immortals.core.das.adaptationmodules.hddrass;

import mil.darpa.immortals.core.api.TestCaseReport;
import mil.darpa.immortals.core.api.TestCaseReportSet;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;
import mil.darpa.immortals.core.das.adaptationmodules.IAdaptationModule;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildBase;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.adaptationtargets.testing.ClassFileCoverageSet;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.core.das.sparql.adaptationtargets.DetermineProvidedFunctionalAspects;
import mil.darpa.immortals.core.das.sparql.deploymentmodel.DetermineHddRassApplicability;
import mil.darpa.immortals.das.context.DasAdaptationContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by awellman@bbn.com on 4/3/18.
 */
public class HddRassAdapter implements IAdaptationModule {
    private List<DetermineHddRassApplicability.HddRassApplicabilityDetails> applicabilityDetails;
    private TestCaseReportSet allTests;
    private TestCaseReportSet failedTests;
    private Map<String, Set<String>> libraryProvidedFunctionalityMap;
    private final List<String> initialClassesToTarget = new LinkedList<>();

    public HddRassAdapter() {

    }

    @Override
    public boolean isApplicable(DasAdaptationContext context) throws Exception {
        boolean rval = false;

        // First get the deployment details to see if library upgrades are being performed
        applicabilityDetails = DetermineHddRassApplicability.select(context);

        // If not, there is nothing hddRASS can do
        if (applicabilityDetails.size() == 0) {
            return false;
        }

        // Then check to see if any tests failed
        allTests = context.getAdaptationTargetTestReports();
        failedTests = allTests.getFailures();

        // If they did not, there is nothing hddRASS can do
        if (failedTests.size() == 0) {
            return false;
        }

        libraryProvidedFunctionalityMap = new HashMap<>();
        for (DetermineHddRassApplicability.HddRassApplicabilityDetails details : applicabilityDetails) {
            // Then, for each application in play

            for (String originalLibrary : details.getLibraryUpgradeMap().keySet()) {
                // For each library it uses
                Set<String> providedLibraryFunctionality;

                // Fetch the library's provided functionality if it has not already been fetched.
                if (!libraryProvidedFunctionalityMap.containsKey(originalLibrary)) {
                    providedLibraryFunctionality = DetermineProvidedFunctionalAspects.select(context, originalLibrary);
                    libraryProvidedFunctionalityMap.put(originalLibrary, providedLibraryFunctionality);
                }
            }
        }

        // Then, out of the provided functionality
        for (Set<String> providedFunctionality : libraryProvidedFunctionalityMap.values()) {

            // For each failed test
            for (TestCaseReport testCaseReport : failedTests) {


                // If the functionality it validates intersects with the functionality provided by the library
                Set<String> testCaseValidatedFunctionality = testCaseReport.getValidatedFunctionality();
                testCaseValidatedFunctionality.retainAll(providedFunctionality);
                if (testCaseValidatedFunctionality.size() > 0) {

                    // And that functionality is optional, remove the test for the tests provided to hddRASS
                    Set<String> appTestCaseValidatedFunctionality = new HashSet<>(testCaseValidatedFunctionality);
                    for (DetermineHddRassApplicability.HddRassApplicabilityDetails details : applicabilityDetails) {
                        appTestCaseValidatedFunctionality.removeAll(details.getRequiredTargetFunctionality());

                        if (appTestCaseValidatedFunctionality.size() > 0) {
                            // And if there are still tests to be used, hddRASS is applicable
                            rval = true;
                        }
                    }

                    // Then add the classes that test touches to the classes to be prioritized initially by hddRASS
                    AdaptationTargetBuildBase base = GradleKnowledgeBuilder.getBuildBase(testCaseReport.getTestCaseTarget());
                    HashMap<String, ClassFileCoverageSet> classFileCoverage = base.getRawBaseProjectData().getBaseTestClassFileCoverage();
                    if (classFileCoverage.containsKey(testCaseReport.getTestCaseIdentifier())) {
                        initialClassesToTarget.addAll(classFileCoverage.get(testCaseReport.getTestCaseIdentifier()).getPartiallyOrFullyCovered().stream().map(t -> t.getIdentifier().replaceAll("/", ".")).collect(Collectors.toSet()));
                    }
                }
            }
        }
        return rval;
    }

    @Override
    public void apply(DasAdaptationContext context) throws Exception {
        for (DetermineHddRassApplicability.HddRassApplicabilityDetails deploymentModelApplicabilityDetails : applicabilityDetails) {
            // For each requested upgrade

            // Collect all tests from the application
            List<TestCaseReport> appTests = allTests.stream().filter(t -> t.getTestCaseTarget().equals(deploymentModelApplicabilityDetails.getAdaptationTarget())).collect(Collectors.toList());

            // Collect all failed tests from the application
            List<TestCaseReport> appFailedTests = failedTests.stream().filter(t -> t.getTestCaseTarget().equals(deploymentModelApplicabilityDetails.getAdaptationTarget())).collect(Collectors.toList());


            // Copy the failed tests to an optional tests variable
            List<TestCaseReport> optionalTests = new LinkedList<>();

            for (TestCaseReport test : appFailedTests) {
                // And for each failed test

                // If no required functionality is validated by the test
                if (!test.getValidatedFunctionality().removeAll(deploymentModelApplicabilityDetails.getRequiredTargetFunctionality())) {
                    // Add it to the optional tests
                    optionalTests.add(test);
                }
            }

            // Take take all tests minus the optional tests as those hddRASS will utilize
            appTests.removeAll(optionalTests);

            // And use them as the test identifiers
            List<String> testsToExecute = appTests.stream().map(TestCaseReport::getTestCaseIdentifier).collect(Collectors.toList());
            AdaptationTargetBuildInstance buildInstance = GradleKnowledgeBuilder.getBuildInstance(deploymentModelApplicabilityDetails.getAdaptationTarget(), context.getAdaptationIdentifer());

            int code = -1;

            // And if specific classes have been targeted, try minimizing them.
            if (initialClassesToTarget.size() > 0) {
                HddRassInitializationObject initializationObject = new HddRassInitializationObject(buildInstance, testsToExecute, initialClassesToTarget);
                HddRassExecuter executer = new HddRassExecuter(context, initializationObject);
                Process p = executer.execute();
                p.waitFor();
                code = p.exitValue();
            }

            // And if validation still fails, try minimizing all of them
            TestCaseReportSet result = buildInstance.executeCleanAndTest(libraryProvidedFunctionalityMap);

            if (result.getFailures().size() > 0) {
                HddRassInitializationObject initializationObject = new HddRassInitializationObject(buildInstance, testsToExecute, null);
                HddRassExecuter executer = new HddRassExecuter(context, initializationObject);
                Process p = executer.execute();
                p.waitFor();
                code = p.exitValue();
            }

            DasOutcome outcome;
            if (code == 0) {
                outcome = DasOutcome.SUCCESS;
            } else {
                System.err.println("HDDRass returned with a non-zero exit code!");
                outcome = DasOutcome.ERROR;
            }
            AdaptationDetails update = new AdaptationDetails(
                    getClass().getName(),
                    outcome,
                    context.getAdaptationIdentifer()
            );
            context.submitAdaptationStatus(update);
        }
    }
}
