package mil.darpa.immortals.das.testcoordinators;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.das.TestCoordinatorExecutionInterface;
import mil.darpa.immortals.testadapter.SubmissionServices;

import javax.ws.rs.core.Response;

/**
 * Created by awellman@bbn.com on 3/22/18.
 */
public class DummyTestCoordinator implements TestCoordinatorExecutionInterface {
    @Override
    public Response execute(SubmissionModel submissionModel) {
        SubmissionServices.getDasSubmitter().submitAdaptationRequest("");
        return Response.ok().build();
    }

    @Override
    public Response execute(SubmissionModel submissionModel, boolean attemptAdaptation) {
        if (attemptAdaptation) {
            SubmissionServices.getDasSubmitter().submitAdaptationRequest("");
        } else {
            SubmissionServices.getDasSubmitter().submitValidationRequest("");
        }
        return Response.ok().build();
    }
}
