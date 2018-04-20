package mil.darpa.immortals.core.das.ll;

import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.result.TestAdapterState;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 3/20/18.
 */
public class TestHarnessAdapterStateContainer {


    public final SubmissionModel submissionModel;
    public final TestAdapterState testAdapterState;
    public final TestHarnessAdapterMediator.ChallengeProblem challengeProblem;
    public boolean initialValidationUpdate = true;

    public TestHarnessAdapterStateContainer(@Nonnull SubmissionModel submissionModel,
                                            @Nonnull TestHarnessAdapterMediator.ChallengeProblem challengeProblem) {
        this.submissionModel = submissionModel;
        this.challengeProblem = challengeProblem;

        // TODO: THis should not be hard coded!
        this.testAdapterState = new TestAdapterState(
                System.currentTimeMillis(),
                submissionModel.sessionIdentifier
        );
    }

    public synchronized String getDisplayableState() {
        String sm;
        String tas;

        try {
            sm = TestHarnessAdapterMediator.gson.toJson(submissionModel);
        } catch (Exception e) {
            e.printStackTrace();
            sm = "PARSE ERROR: " + e.getMessage();
        }

        try {
            tas = TestHarnessAdapterMediator.gson.toJson(testAdapterState);
        } catch (Exception e) {
            e.printStackTrace();
            tas = "PARSE ERROR: " + e.getMessage();
        }

        return "ChallengeProblem: " + challengeProblem.name() + "\n" +
                "SubmissionModel: " + sm + "\n" +
                "TestAdapterState: " + tas + "\n";
    }
}
