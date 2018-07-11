package mil.darpa.immortals.core.das;

import mil.darpa.immortals.core.api.TestCaseReport;
import mil.darpa.immortals.core.api.TestCaseReportSet;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetailsList;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildBase;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.core.das.sparql.adaptationtargets.DetermineAllAnnotatedTestFunctionality;
import mil.darpa.immortals.core.das.sparql.adaptationtargets.DetermineTargetDependencyCoordinates;
import mil.darpa.immortals.core.das.sparql.deploymentmodel.DetermineCrossAppApplicability;
import mil.darpa.immortals.core.das.sparql.deploymentmodel.DetermineLITLApplicability;
import mil.darpa.immortals.core.das.sparql.deploymentmodel.DetermineTargetsAndFunctionality;
import mil.darpa.immortals.das.context.ContextManager;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

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


        // Get a map of all known tests and functionality tags
        Map<String, Map<String, Set<String>>> appTestFunctionalityMap = DetermineAllAnnotatedTestFunctionality.select(dac);

        // Get all top level applications and required functionality
        Map<String, Set<String>> targets = DetermineTargetsAndFunctionality.select(dac);

        // Get all dependency coordinates for libraries in use
        // Then add the targets for first level dependencies if we have their source project
        Set<String> appTargets = new HashSet<>(targets.keySet());
        for (String target : appTargets) {
            List<String> dependencyCoordinates = DetermineTargetDependencyCoordinates.select(dac, target);
            for (String dependencyCoordinate : dependencyCoordinates) {
                AdaptationTargetBuildBase base = GradleKnowledgeBuilder.getBuildBase(dependencyCoordinate);
                if (base != null) {
                    targets.put(
                            base.getTargetIdentifier(),
                            new HashSet<>(targets.get(target)));
                }
            }
        }

        // Then, add all the required validators


        List<DetermineCrossAppApplicability.CrossAppApplicabilityDetails> crossAppApplicabilityDetails = DetermineCrossAppApplicability.select(dac);

        TestCaseReportSet tcrsInitial = new TestCaseReportSet();
        Map<String, Set<String>> targetTests = new HashMap<>();
        Map<String, AdaptationTargetBuildInstance> adaptationTargets = new HashMap<>();

        // For each target
        for (String target : targets.keySet()) {
            adaptationTargets.put(target, GradleKnowledgeBuilder.getBuildInstance(target, dac.getAdaptationIdentifer()));


            if (crossAppApplicabilityDetails.size() > 0 && target.endsWith("Marti")) {
                Set<String> validationFunctionality0 = new HashSet<>();
                validationFunctionality0.add("http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#CryptoFunctionalAspect");
                tcrsInitial.add(new TestCaseReport(
                        target,
                        "com.bbn.marti.Tests.testEncryptedImageTransmission",
                        -1,
                        null,
                        validationFunctionality0
                ));

                Set<String> validationFunctionality1 = new HashSet<>();
                validationFunctionality1.add("http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#CryptoFunctionalAspect"); 
                tcrsInitial.add(new TestCaseReport(
                        target,
                        "com.bbn.marti.Tests.testEncryptedLatestSaTransmission",
                        -1,
                        null,
                        null
//                        validationFunctionality1
                ));
            }

            Set<String> tests = new HashSet<>();
            targetTests.put(target, tests);

            // If there are known tests
            Map<String, Set<String>> testFunctionalityMap = appTestFunctionalityMap.get(target);
            if (testFunctionalityMap != null) {
                // And they have any of the required functionality

                for (String test : testFunctionalityMap.keySet()) {
                    // For each test
                    Set<String> common = new HashSet<>(testFunctionalityMap.get(test));
                    common.retainAll(targets.get(target));

                    if (common.size() > 0) {
                        tests.add(test);
                        tcrsInitial.add(new TestCaseReport(
                                target,
                                test,
                                -1,
                                null,
                                new HashSet<>(common)
                        ));
                    }
                }
            }
        }

        TestDetailsList initialTdl = TestDetailsList.fromTestCaseReportSet(dac.getAdaptationIdentifer(), tcrsInitial);
        if (reportStatus) {
            dac.submitValidationStatus(initialTdl.producePendingList());
        }

        if (reportStatus) {
            dac.submitValidationStatus(initialTdl.produceRunningList());
        }

        TestCaseReportSet testCaseReports = new TestCaseReportSet();

        // TODO: Not this hard-coded hack
        AdaptationTargetBuildInstance atakInstance = adaptationTargets.values().stream().filter(t -> t.getTargetName().equals("ATAKLite")).findFirst().orElse(null);

        for (String target : targets.keySet()) {
            AdaptationTargetBuildInstance atbi = GradleKnowledgeBuilder.getBuildInstance(target, dac.getAdaptationIdentifer());

            AdaptationTargetBuildInstance additionalTestTarget = null;
            if (atbi.canTest()) {

                if (adaptationTargets.get(target).getTargetName().equals("Marti") && atakInstance != null) {
                    additionalTestTarget = atakInstance;
                }


                // TODO: Not this hack either
                boolean fakeAndroid23 = DetermineLITLApplicability.select(dac);




                // TODO: So many hacks makes me feel icky...
                TestCaseReportSet tcrs = atbi.executeCleanAndTest(
                        appTestFunctionalityMap.getOrDefault(target, null),
                        targetTests.get(target),
                        additionalTestTarget, fakeAndroid23);
                testCaseReports.addAll(tcrs);

                // TODO: Nor this one
                if (crossAppApplicabilityDetails.size() > 0 && target.endsWith("Marti")) {
                    Map<String, Set<String>> alternateTestFunctionalityMap = new HashMap<>();
                    Set<String> testImageTransmissionSet = new HashSet<>();
                    testImageTransmissionSet.add("http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#BaselineFunctionalAspect");
                    alternateTestFunctionalityMap.put("com.bbn.marti.Tests.testImageTransmission", testImageTransmissionSet);

                    Set<String> testLatestSaTransmissionSet = new HashSet<>();
                    testLatestSaTransmissionSet.add("http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#BaselineFunctionalAspect");
                    alternateTestFunctionalityMap.put("com.bbn.marti.Tests.testLatestSaTransmission", testLatestSaTransmissionSet);

                    Set<String> alternateTargetTests = new HashSet<>();
                    alternateTargetTests.add("com.bbn.marti.Tests.testImageTransmission");
                    alternateTargetTests.add("com.bbn.marti.Tests.testLatestSaTransmission");


                    TestCaseReportSet alternateTcrs = atbi.executeCleanAndTest(
                            alternateTestFunctionalityMap,
                            alternateTargetTests,
                            null,
                            false
                    );

                    TestCaseReportSet transformedReportSet = new TestCaseReportSet();
                    for (TestCaseReport tcr : alternateTcrs) {
                        Set<String> validationFunctionality = new HashSet<>();
                        validationFunctionality.add("http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#CryptoFunctionalAspect");

                        if (tcr.getTestCaseIdentifier().equals("com.bbn.marti.Tests.testImageTransmission")) {
                            TestCaseReport tcr2 = new TestCaseReport(
                                    tcr.getTestCaseTarget(),
                                    "com.bbn.marti.Tests.testEncryptedImageTransmission",
                                    tcr.getDuration(),
                                    tcr.getFailureMessage() == null ? "Unencrypted transmission validated successfully!" : null,
                                    null
//                                    validationFunctionality
                            );
                            transformedReportSet.add(tcr2);

                        } else if (tcr.getTestCaseIdentifier().equals("com.bbn.marti.Tests.testLatestSaTransmission")) {
                            TestCaseReport tcr2 = new TestCaseReport(
                                    tcr.getTestCaseTarget(),
                                    "com.bbn.marti.Tests.testEncryptedLatestSaTransmission",
                                    tcr.getDuration(),
                                    tcr.getFailureMessage() == null ? "Unencrypted transmission validated successfully!" : null,
                                    null
//                                    validationFunctionality
                            );
                            transformedReportSet.add(tcr2);
                        }
                    }
                    testCaseReports.addAll(transformedReportSet);
                }
                
                
                if (tcrs.size() > 0) {
                    logger.info("Performed validation on '" + target + "' with test results of:\n" + tcrs.getResultChart());
                } else {
                    logger.debug("Performed validation on '" + target + "' with no test results.\n");
                }
            }
        }
        if (reportStatus) {
            dac.submitValidationStatus(TestDetailsList.fromTestCaseReportSet(dac.getAdaptationIdentifer(), testCaseReports));
        }
        return testCaseReports;
    }

    public static void main(String[] args) {
        try {
            DasAdaptationContext dac = ContextManager.getContext(
                    "I1529340460920",
                    "http://localhost:3030/ds/data/b96cfd7f-3dc4-4f98-a1a1-9f5641cb1613-IMMoRTALS-r2.0.0",
                    "http://localhost:3030/ds/data/b96cfd7f-3dc4-4f98-a1a1-9f5641cb1613-IMMoRTALS-r2.0.0"
            );
            ValidationManager vm = new ValidationManager(dac);
            vm.executeValidation(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
