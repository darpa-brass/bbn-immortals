package mil.darpa.immortals.core.das.adaptationmodules.hddrass;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.TestCaseReport;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetailsList;
import mil.darpa.immortals.core.das.sparql.deploymentmodel.DetermineTargets;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by awellman@bbn.com on 4/6/18.
 */
public class Hacks {

    private static final String MARTI_DM_IDENTIFIER = "http://darpa.mil/immortals/ontology/r2.0.0/cp2#ClientServerEnvironment.MartiServer";
    private static final String ATAKLITE_DM_IDENTIFIER = "http://darpa.mil/immortals/ontology/r2.0.0/cp2#ClientServerEnvironment.ClientDevice1";
    private static final String MARTI_COORDINATE_IDENTIFIER = "Marti";
    private static final String ATAKLITE_COORDINATE_IDENTIFIER = "ATAKLite";

    private static final Map<String, String> coordinatesToDeploymentModelMap = new HashMap<>();
    private static final Map<String, String> deploymentModelToCoordinatesMap = new HashMap<>();

    private static Logger logger = LoggerFactory.getLogger("Hacks");

    static {
        coordinatesToDeploymentModelMap.put(MARTI_COORDINATE_IDENTIFIER, MARTI_DM_IDENTIFIER);
        coordinatesToDeploymentModelMap.put(ATAKLITE_COORDINATE_IDENTIFIER, ATAKLITE_DM_IDENTIFIER);
        deploymentModelToCoordinatesMap.put(MARTI_DM_IDENTIFIER, MARTI_COORDINATE_IDENTIFIER);
        deploymentModelToCoordinatesMap.put(ATAKLITE_DM_IDENTIFIER, ATAKLITE_COORDINATE_IDENTIFIER);
    }

    public static String normnalizeIdentifier(@Nonnull String shortIdentifier) {
        String rval = null;

        if (shortIdentifier.equals("core")) {
            rval = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("shared/modules/core").toString();

        } else if (shortIdentifier.equals("ElevationApi-1")) {
            rval = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("shared/modules/dfus/ElevationApi-1").toString();

        } else if (shortIdentifier.equals("ElevationApi-2")) {
            rval = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("shared/modules/dfus/ElevationApi-2").toString();

        } else if (shortIdentifier.equals("TakServerDataManager")) {
            rval = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("shared/modules/dfus/TakServerDataManager").toString();

        } else if (shortIdentifier.equals("Marti")) {
            rval = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/server/Marti").toString();

        } else if (shortIdentifier.equals("ATAKLite")) {
            rval = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/client/ATAKLite").toString();

        } else if (shortIdentifier.equals("ThirdPartyLibAnalysisAndroidApp")) {
            rval = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/examples/ThirdPartyLibAnalysisAndroidApp").toString();

        } else if (shortIdentifier.equals("ThirdPartyLibAnalysisJavaApp")) {
            rval = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/examples/ThirdPartyLibAnalysisJavaApp").toString();
        }

        if (rval != null) {
            logger.warn("Short Identifier '" + shortIdentifier + "' Converted to normalized identifier '" + rval + "'.");
            return rval;
        }
        return shortIdentifier;
    }

    // TODO: This linkage shouldn't need this hack...
    public static String deploymentModelIdentifierToAbsoluteIdentifier(String deploymentModelIdentifier) {
        return deploymentModelToCoordinatesMap.get(deploymentModelIdentifier);
    }

    // TODO: This linkage shouldn't need this hack...
    public static String absoluteIdentifierToDeploymentModelIdentifier(String coordinates) {
        return coordinatesToDeploymentModelMap.get(coordinates);
    }

    public static void injectMissingTestFunctionalityCP3PLUGFirstValidation(DasAdaptationContext dac, Map<String, Map<String, Set<String>>> functionality) {
        Set<String> targets = DetermineTargets.select(dac);

        String app_path = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/examples/ThirdPartyLibAnalysisAndroidApp").toString();

        if (targets.contains(app_path)) {
            Map<String, Set<String>> appFunctionality = functionality.get(app_path);

            if (appFunctionality == null) {
                appFunctionality = new HashMap<>();
                functionality.put(app_path, appFunctionality);
            }

            Set<String> shouldPassWith003ButFailWith006Tags = appFunctionality.get("mil.darpa.immortals.examples.tests.DropboxInstrumentedTest.shouldPassWith003ButFailWith006");
            if (shouldPassWith003ButFailWith006Tags == null) {
                shouldPassWith003ButFailWith006Tags = new HashSet<>();
                appFunctionality.put("mil.darpa.immortals.examples.tests.DropboxInstrumentedTest.shouldPassWith003ButFailWith006", shouldPassWith003ButFailWith006Tags);
            }
            shouldPassWith003ButFailWith006Tags.add("http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#BaselineFunctionalAspect");

            Set<String> dropboxUnitTestTags = appFunctionality.get("mil.darpa.immortals.examples.tests.DropboxInstrumentedTest.dropboxUnitTest");
            if (dropboxUnitTestTags == null) {
                dropboxUnitTestTags = new HashSet<>();
                appFunctionality.put("mil.darpa.immortals.examples.tests.DropboxInstrumentedTest.dropboxUnitTest", dropboxUnitTestTags);
            }
            dropboxUnitTestTags.add("http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#VulnerabilityDropboxFunctionalAspect");
        }
    }

    public static TestDetailsList getMissingExpectedTestsCP3PLUGFirstValidation(DasAdaptationContext dac) {
        Set<String> targets = DetermineTargets.select(dac);

        String app_path = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/examples/ThirdPartyLibAnalysisAndroidApp").toString();

        if (targets.contains(app_path)) {
            List<String> funcs = new LinkedList<>();
            funcs.add("http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#BaselineFunctionalAspect");
            TestCaseReport tcr = new TestCaseReport(
                    app_path,
                    "mil.darpa.immortals.examples.tests.DropboxInstrumentedTest.shouldPassWith003ButFailWith006",
                    -1,
                    null,
                    funcs);

            List<String> funcs2 = new LinkedList<>();
            funcs2.add("http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#VulnerabilityDropboxFunctionalAspect");
            TestCaseReport tcr2 = new TestCaseReport(
                    app_path,
                    "mil.darpa.immortals.examples.tests.DropboxInstrumentedTest.dropboxUnitTest",
                    -1,
                    null,
                    funcs2);

            TestDetailsList tdl = new TestDetailsList();
            tdl.add(new TestDetails(tcr, dac.getAdaptationIdentifer()));
            tdl.add(new TestDetails(tcr2, dac.getAdaptationIdentifer()));
            return tdl;
        }
        return null;
    }
}
