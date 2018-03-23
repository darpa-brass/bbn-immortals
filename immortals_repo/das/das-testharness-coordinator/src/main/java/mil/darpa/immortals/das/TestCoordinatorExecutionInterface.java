package mil.darpa.immortals.das;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;

import javax.ws.rs.core.Response;

/**
 * Created by awellman@bbn.com on 10/19/17.
 */
public interface TestCoordinatorExecutionInterface {

    /**
     * No reply will be returned to LL until this method returns!
     *
     * @param submissionModel
     */
    Response execute(SubmissionModel submissionModel);

    /**
     * No reply will be returned to LL until this method returns!
     *
     * @param submissionModel
     */
    Response execute(SubmissionModel submissionModel, boolean attemptAdaptation);
}
