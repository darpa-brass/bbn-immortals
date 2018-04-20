package mil.darpa.immortals.das.context;


import mil.darpa.immortals.core.api.TestCaseReportSet;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 10/26/17.
 */
public class ContextManager {

    public synchronized static DasAdaptationContext getContext(@Nonnull String adaptationIdentifier, @Nonnull String deploymentModelUri, @Nonnull String knowledgeUri) {
        return new DasAdaptationContext(adaptationIdentifier, deploymentModelUri, knowledgeUri);
    }

    public synchronized static void setAdaptationTargetState(DasAdaptationContext dac, TestCaseReportSet testReports) {
        dac.setAdaptationTargetTestReports(testReports);
    }
}
