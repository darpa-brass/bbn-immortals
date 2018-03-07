package mil.darpa.immortals.core.api.validation.results;

import mil.darpa.immortals.core.api.ll.phase1.TestResult;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 10/24/16.
 */
public class ValidationResults {

    public final long testDurationMS;

    public final LinkedList<TestResult> results;

    public ValidationResults(List<TestResult> results, long testDurationMS) {
        this.results = new LinkedList<>(results);
        this.testDurationMS = testDurationMS;
    }
}
