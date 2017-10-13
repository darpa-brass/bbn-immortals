package mil.darpa.immortals.core.das.ll;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;

/**
 * Created by awellman@bbn.com on 10/10/17.
 */
public interface TestHarnessDasPerturbationInterface {

    /**
     * Starts the adaptation for P2CP1. A return value is expected immediately to provide an acknowledgement to the 
     * test harness.
     * 
     * Send an updated {@link AdaptationDetails} to {@link TestHarnessAdapterMediator#updateDasStatus(AdaptationDetails)}
     * to update the status.  Handling of validation or a terminal state will be handled based on the value of 
     * {@link AdaptationDetails#dasOutcome}
     * @param submissionModel The input submission model
     * @return The initial state to respond to the Test Harness POST with
     */
    AdaptationDetails startP2CP1(SubmissionModel submissionModel);


    /**
     * Starts the adaptation for P2CP2. A return value is expected immediately to provide an acknowledgement to the 
     * test harness.
     *
     * Send an updated {@link AdaptationDetails} to {@link TestHarnessAdapterMediator#updateDasStatus(AdaptationDetails)}
     * to update the status.  Handling of validation or a terminal state will be handled based on the value of 
     * {@link AdaptationDetails#dasOutcome}
     * @param submissionModel The input submission model
     * @return The initial state to respond to the Test Harness POST with
     */
    AdaptationDetails startP2CP2(SubmissionModel submissionModel);

    /**
     * Starts the adaptation for P2CP3. A return value is expected immediately to provide an acknowledgement to the 
     * test harness.
     *
     * Send an updated {@link AdaptationDetails} to {@link TestHarnessAdapterMediator#updateDasStatus(AdaptationDetails)}
     * to update the status.  Handling of validation or a terminal state will be handled based on the value of 
     * {@link AdaptationDetails#dasOutcome}
     * @param submissionModel The input submission model
     * @return The initial state to respond to the Test Harness POST with
     */
    AdaptationDetails startP2CP3(SubmissionModel submissionModel);
}
