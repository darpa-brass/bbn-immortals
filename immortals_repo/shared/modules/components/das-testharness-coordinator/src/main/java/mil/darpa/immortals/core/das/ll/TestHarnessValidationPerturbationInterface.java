package mil.darpa.immortals.core.das.ll;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetails;

import java.util.List;

/**
 * Created by awellman@bbn.com on 10/10/17.
 */
public interface TestHarnessValidationPerturbationInterface {
    List<TestDetails> startP2CP1(SubmissionModel submissionModel);

    List<TestDetails> startP2CP2(SubmissionModel submissionModel);

    List<TestDetails> startP2CP3(SubmissionModel submissionModel);
}
