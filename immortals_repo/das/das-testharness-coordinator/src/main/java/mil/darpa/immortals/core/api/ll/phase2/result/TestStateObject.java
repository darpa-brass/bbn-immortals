package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Result;
import mil.darpa.immortals.core.api.annotations.Unstable;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;

import java.util.LinkedList;

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

    public TestStateObject(String testIdentifier, String intent, TestOutcome desiredStatus, TestOutcome actualStatus, TestDetails details) {
        this.testIdentifier = testIdentifier;
        this.intent = intent;
        this.desiredStatus = desiredStatus;
        this.actualStatus = actualStatus;
        this.details = details;
    }
}
