package mil.darpa.immortals.core.das.ll;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;

/**
 * Created by awellman@bbn.com on 9/28/17.
 */
public interface TestHarnessPerturbationInterface {

    /**
     * @param submissionModel The perturbation model to submit
     * @return The new state if a scenario could be started, null if one is already running
     */
    void executeP2CP1(SubmissionModel submissionModel);

    void executeP2CP2(SubmissionModel submissionModel);

    void executeP2CP3(SubmissionModel submissionModel);
}
