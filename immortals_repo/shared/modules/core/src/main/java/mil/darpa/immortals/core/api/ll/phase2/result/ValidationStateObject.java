package mil.darpa.immortals.core.api.ll.phase2.result;

import mil.darpa.immortals.core.api.annotations.Description;
import mil.darpa.immortals.core.api.annotations.Result;
import mil.darpa.immortals.core.api.ll.phase2.result.status.VerdictOutcome;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
@Result
@Description("The current state of intent satisfaction validation")
public class ValidationStateObject {

    @Result
    @Description("The outcome of the intent preservation")
    public VerdictOutcome verdictOutcome;
    @Result
    @Description("The tests executed to support the verdict outcome")
    public LinkedList<TestStateObject> executedTests;

    public ValidationStateObject() {
    }

    public ValidationStateObject(VerdictOutcome verdictOutcome, LinkedList<TestStateObject> executedTests) {
        this.verdictOutcome = verdictOutcome;
        this.executedTests = executedTests;
    }
}
