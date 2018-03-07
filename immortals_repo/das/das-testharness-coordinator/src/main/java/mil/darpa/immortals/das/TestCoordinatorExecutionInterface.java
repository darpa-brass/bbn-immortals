package mil.darpa.immortals.das;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;

/**
 * Created by awellman@bbn.com on 10/19/17.
 */
public interface TestCoordinatorExecutionInterface {

    /**
     * No reply will be returned to LL until this method returns!
     * @param submissionModel
     */
    AdaptationDetails execute(SubmissionModel submissionModel);
}
