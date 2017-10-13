package mil.darpa.immortals.core.api.ll.phase1;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
public class ValidationState {

    public final Status overallIntentStatus;
    public final LinkedList<TestDetails> executedTests;

    public ValidationState(Status overallIntentStatus, LinkedList<TestDetails> executedTests) {
        this.overallIntentStatus = overallIntentStatus;
        this.executedTests = executedTests;
    }
}
