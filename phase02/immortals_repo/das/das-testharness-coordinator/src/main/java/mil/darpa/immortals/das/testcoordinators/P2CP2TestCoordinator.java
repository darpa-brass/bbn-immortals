package mil.darpa.immortals.das.testcoordinators;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 1/5/18.
 */
public class P2CP2TestCoordinator extends AbstractTestCoordinator {
    @Nonnull
    @Override
    List<String> validateSubmissionModel(@Nonnull SubmissionModel submissionModel) throws Exception {
        List<String> rval = new LinkedList<>();

        if (submissionModel.martiServerModel != null) {
            if (submissionModel.martiServerModel.requirements != null) {
                rval.add("martiServerModel cannot define its own requirements for CP2!");
            }
        }

        if (submissionModel.atakLiteClientModel != null) {
            if (submissionModel.atakLiteClientModel.requirements != null) {
                rval.add("atakLiteClientModel cannot define its own requirements for CP2!");
            }
        }

        return rval;
    }

    @Override
    void setupChallengeProblem(@Nonnull SubmissionModel submissionModel) throws Exception {
        // Nothing to set up
    }
}
