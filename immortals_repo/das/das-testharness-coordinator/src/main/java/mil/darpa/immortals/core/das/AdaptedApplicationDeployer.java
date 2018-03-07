package mil.darpa.immortals.core.das;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 9/29/17.
 */
public class AdaptedApplicationDeployer {

    public AdaptedApplicationDeployer() {
    }

    public synchronized List<TestDetails> startP2CP1(SubmissionModel submissionModel) {
        if (ImmortalsConfig.getInstance().debug.isUseMockApplicationDeployment()) {
            return Mock.appDeploymentSubmission(submissionModel, "P2CP1");
        } else {
            TestDetails td = new TestDetails();
            td.currentState = TestOutcome.ERROR;
            ArrayList<TestDetails> tdl = new ArrayList<>(1);
            tdl.add(td);
            return tdl;
        }
    }

    public synchronized List<TestDetails> startP2CP2(SubmissionModel submissionModel) {
        if (ImmortalsConfig.getInstance().debug.isUseMockApplicationDeployment()) {
            return Mock.appDeploymentSubmission(submissionModel, "P2CP2");
        } else {
            TestDetails td = new TestDetails();
            td.currentState = TestOutcome.ERROR;
            ArrayList<TestDetails> tdl = new ArrayList<>(1);
            tdl.add(td);
            return tdl;
        }
    }

    public synchronized List<TestDetails> startP2CP3(SubmissionModel submissionModel) {
        if (ImmortalsConfig.getInstance().debug.isUseMockApplicationDeployment()) {
            return Mock.appDeploymentSubmission(submissionModel, "P2CP3");
        } else {
            TestDetails td = new TestDetails();
            td.currentState = TestOutcome.ERROR;
            ArrayList<TestDetails> tdl = new ArrayList<>(1);
            tdl.add(td);
            return tdl;
        }
    }
}
