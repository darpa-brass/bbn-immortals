package mil.darpa.immortals.das.testcoordinators;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.das.TestCoordinatorExecutionInterface;
import mil.darpa.immortals.testadapter.SubmissionServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

/**
 * Created by awellman@bbn.com on 3/22/18.
 */
public class DummyTestCoordinator implements TestCoordinatorExecutionInterface {
    
    Logger logger = LoggerFactory.getLogger(DummyTestCoordinator.class);

    @Override
    public Response execute(SubmissionModel submissionModel, boolean attemptAdaptation) {
        if (attemptAdaptation) {
            logger.debug("Submitting empty adaptation request.");
            SubmissionServices.getDasSubmitter().submitAdaptationRequest("");
        } else {
            logger.debug("Submitting empty validation request.");
            SubmissionServices.getDasSubmitter().submitValidationRequest("");
        }
        return Response.ok().build();
    }
}
