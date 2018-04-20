package mil.darpa.immortals.analysis.adaptationtargets;

/**
 * Created by awellman@bbn.com on 4/11/18.
 */
public class ImmortalsGradleTestData {
    private final String[] buildToolValidationParameters;
    private final String testResultXmlSubdirectory;
    private final String testCoverageReportXmlFileSubpath;

    public ImmortalsGradleTestData(String[] buildToolValidationParameters, String testResultXmlSubdirectory, String testCoverageReportXmlFileSubpath) {
        this.buildToolValidationParameters = buildToolValidationParameters;
        this.testResultXmlSubdirectory = testResultXmlSubdirectory;
        this.testCoverageReportXmlFileSubpath = testCoverageReportXmlFileSubpath;
    }

    public String[] getTestBuildToolParameters() {
        return buildToolValidationParameters.clone();
    }

    public String getTestResultXmlSubdirectory() {
        return testResultXmlSubdirectory;
    }

    public String getTestCoverageReportXmlFileSubpath() {
        return testCoverageReportXmlFileSubpath;
    }
}
