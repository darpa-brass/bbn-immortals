package mil.darpa.immortals.core.das.adaptationtargets.testing;

import mil.darpa.immortals.core.api.TestCaseReport;
import mil.darpa.immortals.core.api.TestCaseReportSet;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * Created by awellman@bbn.com on 4/12/18.
 */
public class XmlParser {

    private static Logger logger = LoggerFactory.getLogger(XmlParser.class);

    private static TestCaseReport testCaseReportFromElement(@Nonnull Element testCaseElement, @Nonnull String testCaseTarget, @Nullable Map<String, Set<String>> testFunctionalityMappings) {
        String testCaseIdentifier = testCaseElement.attributeValue("classname") + "." + testCaseElement.attributeValue("name");

        Set<String> validatedFunctionality = testFunctionalityMappings == null ? null : testFunctionalityMappings.get(testCaseIdentifier);

        double time = Double.valueOf(testCaseElement.attributeValue("time"));
        String failureMessage = null;

        Node failureNode = testCaseElement.selectSingleNode("failure");
        if (failureNode == null) {
            failureNode = testCaseElement.selectSingleNode("error");
        }
        if (failureNode != null) {
            if (failureNode instanceof Element) {
                Element e = (Element) failureNode;
                failureMessage = e.attributeValue("message");
            }
        }
        return new TestCaseReport(testCaseTarget, testCaseIdentifier, time, failureMessage, validatedFunctionality);
    }

    public static TestCaseReportSet getTestResultsFromFlatDirectory(@Nonnull File testResultDirectory, @Nonnull String appIdentifier, @Nullable Map<String, Set<String>> testFunctionalityMappings) throws DocumentException {
        File[] files = testResultDirectory.listFiles();
        TestCaseReportSet testCaseReports = new TestCaseReportSet();
        if (files != null) {
            for (File f : files) {
                if (f.getName().endsWith(".xml")) {
                    Set<TestCaseReport> newReports = XmlParser.createTestSuiteReportFromFile(f, appIdentifier, testFunctionalityMappings);
                    testCaseReports.addAll(newReports);
                }
            }
        }
        return testCaseReports;
    }

    private static TestCaseReportSet createTestSuiteReportFromFile(@Nonnull File filepath, @Nonnull String appIdentifier, @Nullable Map<String, Set<String>> testFunctionalityMappings) throws DocumentException {
        SAXReader sr = new SAXReader();

        TestCaseReportSet testCaseReports = new TestCaseReportSet();

        Document doc = sr.read(filepath);
        Element report = doc.getRootElement();
        String name = report.attributeValue("name");
        int suiteTestCount = Integer.valueOf(report.attributeValue("tests"));
        int suiteSkippedCount = Integer.valueOf(report.attributeValue("skipped"));
        int suiteFailureCount = Integer.valueOf(report.attributeValue("failures"));
        int suiteErrorCount = Integer.valueOf(report.attributeValue("errors"));

        List<Element> testCaseNodes = doc.selectNodes("/testsuite/testcase");

        for (Element node : testCaseNodes) {
            testCaseReports.add(testCaseReportFromElement(node, appIdentifier, testFunctionalityMappings));
        }

        return testCaseReports;
    }

    private static int getCovered(@Nonnull List<Element> counters, @Nonnull String type, @Nonnull String key) {
        Optional<Element> counter = counters.stream().filter(t -> type.equals(t.attributeValue("type"))).findAny();
        if (counter.isPresent()) {
            String value = counter.get().attributeValue(key);
            if (value == null) {
                return 0;
            } else {
                return Integer.valueOf(value);
            }

        } else {
            return 0;
        }
    }


    static ClassFileCoverage createClassFileCoverageFromElement(Element ce) {
        List<Element> counters = ce.selectNodes("counter");

        ClassFileCoverage cc = new ClassFileCoverage(
                ce.attributeValue("name"),
                getCovered(counters, "INSTRUCTION", "covered"),
                getCovered(counters, "INSTRUCTION", "missed"),
                getCovered(counters, "BRANCH", "covered"),
                getCovered(counters, "BRANCH", "missed"),
                getCovered(counters, "LINE", "covered"),
                getCovered(counters, "LINE", "missed"),
                getCovered(counters, "COMPLEXITY", "covered"),
                getCovered(counters, "COMPLEXITY", "missed"),
                getCovered(counters, "METHOD", "covered"),
                getCovered(counters, "METHOD", "missed")
        );
        return cc;
    }

    public static ClassFileCoverageSet createClassFileCoverageReportsFromFile(File filepath) throws Exception {
        HashSet<ClassFileCoverage> childClassCoverage = new HashSet<>();
        HashMap<String, ClassFileCoverage> classCoverage = new HashMap<>();

        SAXReader sr = new SAXReader();
        sr.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                if (systemId.contains("report.dtd")) {
                    return new InputSource(new StringReader(""));
                }
                return null;
            }
        });

        Document doc = sr.read(filepath);
        Element report = doc.getRootElement();
        String name = report.attributeValue("name");

        List<Element> classNodes = doc.selectNodes("/report/package/class");

        for (Element ce : classNodes) {
            String className = ce.attributeValue("name");
            List<Element> counters = ce.selectNodes("/counter");

            ClassFileCoverage cc = createClassFileCoverageFromElement(ce);

            if (className.contains("$")) {
                childClassCoverage.add(cc);
            } else {
                classCoverage.put(className, cc);
            }
        }

        // Then add the coverage for child classes contained in a parent class to the parent class
        for (ClassFileCoverage childClass : childClassCoverage) {
            String parentClass = childClass.getIdentifier().substring(0, childClass.getIdentifier().indexOf("$"));

            ClassFileCoverage parentClassCoverage = classCoverage.get(parentClass);
            ClassFileCoverage newCoverage = new ClassFileCoverage(
                    parentClassCoverage.getIdentifier(),
                    parentClassCoverage.getInstructionsCovered() + childClass.getInstructionsCovered(),
                    parentClassCoverage.getInstructionsMissed() + childClass.getInstructionsMissed(),
                    parentClassCoverage.getBranchesCovered() + childClass.getBranchesCovered(),
                    parentClassCoverage.getBranchesMissed() + childClass.getBranchesMissed(),
                    parentClassCoverage.getLinesCovered() + childClass.getLinesCovered(),
                    parentClassCoverage.getLinesMissed() + childClass.getLinesMissed(),
                    parentClassCoverage.getComplexityCovered() + childClass.getComplexityCovered(),
                    parentClassCoverage.getComplexityMissed() + childClass.getComplexityMissed(),
                    parentClassCoverage.getMethodsCovered() + childClass.getMethodsCovered(),
                    parentClassCoverage.getMethodsMissed() + childClass.getMethodsMissed()
            );
            classCoverage.replace(parentClass, newCoverage);
        }
        return new ClassFileCoverageSet(classCoverage.values());
    }

}
