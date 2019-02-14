package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Result;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
@Result
@Description("The current state of an intent test validation")
public class TestStateObject {
    @Result
    @Description("An identifier for the test")
    public String testIdentifier;
    @Result
    @Description("The intent the test validates")
    public String intent;
    @Result
    @Description("The desired outcome for the test. Will always be 'COMPLETE' for Phase 2")
    public TestOutcome desiredStatus;
    @Result
    @Description("The current state for the test")
    public TestOutcome actualStatus;
    @Result
    @Description("Details relating to the resultant test state.")
    @Unstable
    public TestDetails details;

    public TestStateObject() {
    }

    public TestStateObject(String testIdentifier, TestOutcome desiredStatus, TestOutcome actualStatus, TestDetails details) {
        this.testIdentifier = testIdentifier;
        this.desiredStatus = desiredStatus;
        this.actualStatus = actualStatus;
        this.details = details;
        if (details.validatedFunctionality == null || details.validatedFunctionality.size() == 0) {
            this.intent = "UNDEFINED";
        } else {
            this.intent = String.join("+", details.validatedFunctionality);
        }
    }

    public TestStateObject(@Nonnull TestDetails testDetails) {
        this.testIdentifier = testDetails.testIdentifier;

        if (testDetails.validatedFunctionality == null || testDetails.validatedFunctionality.size() == 0) {
            this.intent = "UNDEFINED";
        } else {
            this.intent = String.join("+", testDetails.validatedFunctionality);
        }
        this.desiredStatus = TestOutcome.COMPLETE_PASS;
        this.actualStatus = testDetails.currentState;
        this.details = testDetails;

    }

    public void update(@Nonnull TestDetails testDetails) {
        if (!testIdentifier.equals(testDetails.testIdentifier)) {
            ImmortalsErrorHandler.reportFatalError("Cannot update a TestStateObject using TestData from a different Test!");
        }

//        if (actualStatus != TestOutcome.PENDING && actualStatus != TestOutcome.RUNNING) {
//            if (testDetails.currentState == TestOutcome.PENDING || testDetails.currentState == TestOutcome.RUNNING) {
//                ImmortalsErrorHandler.reportFatalError("Unexpected transition of test '" + testDetails.testIdentifier
//                        + "' state from terminal state '" + actualStatus.name() +
//                        " to non-terminal state '" + testDetails.currentState.name() + "'!");
//            } else {
//                ImmortalsErrorHandler.reportFatalError("Unexpected transition of test '" + testDetails.testIdentifier
//                        + "' state from terminal state '" + actualStatus.name() +
//                        " to another terminal state '" + testDetails.currentState.name() + "'!");
//            }
//        }

        this.actualStatus = testDetails.currentState;
        this.details = testDetails;
    }
}
