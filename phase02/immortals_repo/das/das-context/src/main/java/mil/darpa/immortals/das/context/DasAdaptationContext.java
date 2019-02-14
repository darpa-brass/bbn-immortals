package mil.darpa.immortals.das.context;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetailsList;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetails;
import mil.darpa.immortals.core.api.TestCaseReportSet;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetailsList;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 10/25/17.
 */
public class DasAdaptationContext {

    private String adaptationIdentifier;
    private String deploymentModelUri;
    private String knowledgeUri;
    private TestCaseReportSet adaptationTargetTestReports = null;

    public DasAdaptationContext() {
    }

    DasAdaptationContext(@Nonnull String adaptationIdentifier, @Nonnull String deploymentModelUri, @Nonnull String knowledgeUri) {
        this.adaptationIdentifier = adaptationIdentifier;
        this.deploymentModelUri = deploymentModelUri;
        this.knowledgeUri = knowledgeUri;
    }

    public String getDeploymentModelUri() {
        return deploymentModelUri;
    }

    public String getKnowldgeUri() {
        return knowledgeUri;
    }

    public String getAdaptationIdentifer() {
        return adaptationIdentifier;
    }

    public Path getAdaptationDirectory() {
        return ImmortalsConfig.getInstance().globals.getAdaptationWorkingDirectory(adaptationIdentifier);
    }

    public Path getAdaptationLogDirectory() {
        return ImmortalsConfig.getInstance().globals.getAdaptationLogDirectory(adaptationIdentifier);
    }

    /**
     * Submits the current adaptation status to the DAS.
     * <p>
     * WARNING: This is a wrapper to a session-agnostic call, so adaptationDetails must contain an adaptationIdentifier!
     *
     * @param adaptationDetails
     */
    public void submitAdaptationStatus(AdaptationDetails adaptationDetails) {
        AdaptationDetailsList adl = new AdaptationDetailsList();
        adl.add(adaptationDetails);
        TestAdapterSubmitter.updateAdaptationStatus(adl);
    }

    public void submitValidationStatus(TestDetailsList testDetails) {
        TestAdapterSubmitter.updateValidationStatus(new TestDetailsList(testDetails));
    }

    /**
     * Submits an error to the TestHarness asynchronously which is expected to Ctrl-C the DAS.
     * Does not throw an exception itself.
     * Try to make it verbose to simplify debugging!
     *
     * @param error The error String to report
     */
    public void reportFatalError(String error) {
        ImmortalsErrorHandler.reportFatalError(error);
    }

    /**
     * Submits an exception to the TestHarness asynchronously which is expected to Ctrl-C the DAS.
     * Does not throw an exception itself.
     *
     * @param t The exception to report
     */
    public void reportFatalException(Throwable t) {
        ImmortalsErrorHandler.reportFatalException(t);
    }

    void setAdaptationTargetTestReports(@Nonnull TestCaseReportSet adaptationTargetTestReports) {
        this.adaptationTargetTestReports = new TestCaseReportSet(adaptationTargetTestReports);
    }

    public TestCaseReportSet getAdaptationTargetTestReports() {
        return adaptationTargetTestReports;
    }
}
