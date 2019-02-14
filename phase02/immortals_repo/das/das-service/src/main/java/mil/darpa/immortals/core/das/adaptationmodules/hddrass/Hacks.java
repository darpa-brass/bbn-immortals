package mil.darpa.immortals.core.das.adaptationmodules.hddrass;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.adaptationmodules.crossappdependencies.Res;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by awellman@bbn.com on 4/6/18.
 */
public class Hacks {

    private static Logger logger = LoggerFactory.getLogger("Hacks");

    public static void injectMissingTestsAndFunctionality(Map<String, Map<String, Set<String>>> functionality) {
        try {
            Map<String, Set<String>> bbnDetectedTests = GradleKnowledgeBuilder.getAllTargetTests();

            for (String artifactIdentifier : bbnDetectedTests.keySet()) {

                Map<String, Set<String>> appFunctionality = functionality.get(artifactIdentifier);
                if (appFunctionality == null) {
                    logger.debug("'" + artifactIdentifier + "' not analyzed by KRGP! Falling back on BBN Coverage Analysis!");
                    appFunctionality = new HashMap<>();
                    functionality.put(artifactIdentifier, appFunctionality);
                }

                for (String testIdentifier : bbnDetectedTests.get(artifactIdentifier)) {
                    Set<String> testFunctionality = appFunctionality.get(testIdentifier);
                    if (testFunctionality == null) {
                        logger.debug("'" + artifactIdentifier + "' test '" + testIdentifier + "' not analyzed by KRGP! Falling back on BBN Coverage Analysis!");
                        testFunctionality = new HashSet<>();
                        appFunctionality.put(testIdentifier, testFunctionality);

                        if (testIdentifier.equals("mil.darpa.immortals.examples.tests.DropboxInstrumentedTest.dropboxUnitTest")) {
                            testFunctionality.add("http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#VulnerabilityDropboxFunctionalAspect");

                        } else {
                            testFunctionality.add("http://darpa.mil/immortals/ontology/r2.0.0/mil/darpa/immortals/ontology#BaselineFunctionalAspect");
                        }
                    }
                }

            }

            String app_path = ImmortalsConfig.getInstance().globals.getImmortalsRoot().resolve("applications/examples/ThirdPartyLibAnalysisAndroidApp").toString();
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
        } catch (IOException e) {
            ImmortalsErrorHandler.reportFatalException(e);
            throw new RuntimeException(e);
        }
    }

    private static String getDfuResourceUri(Path sourceFilepath) throws IOException {
        Model m = ModelFactory.createDefaultModel();
        InputStream is = new FileInputStream(sourceFilepath.toFile());
        RDFDataMgr.read(m, is, Lang.TTL);

        List<Resource> resources = m.listResourcesWithProperty(RDF.type, m.getResource(Res.DFU_INSTANCE.uri)).toList();

        if (resources.size() != 1) {
            throw new RuntimeException("There should only be one DfuInstance in the JavaX Dfu file!");
        }

        return resources.get(0).getURI();
    }

    public static String getJavaxDfuInstanceUri() throws Exception {
        String rval = getDfuResourceUri(
                ImmortalsConfig.getInstance().extensions.krgp.getTtlTargetDirectory().resolve(
                        "JavaxCrypto/structures/dfus/CipherImplJavaxCrypto.class-DFU.ttl"));
//                .replaceAll(Prefix.IMMoRTALS_dfu_instance.uri, Prefix.IMMoRTALS_dfu_instance.name() + ":");
        return rval;
    }

    public static String getBcDfuInstanceUri() throws Exception {
        String rval = getDfuResourceUri(
                ImmortalsConfig.getInstance().extensions.krgp.getTtlTargetDirectory().resolve(
                        "BouncyCastleCipher/structures/dfus/CipherImplBouncyCrypto.class-DFU.ttl"));
//                .replaceAll(Prefix.IMMoRTALS_dfu_instance.uri, Prefix.IMMoRTALS_dfu_instance.name() + ":");
        return rval;
    }
}
