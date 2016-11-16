package mil.darpa.immortals.analytics.validators.result;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 10/24/16.
 */
public class ValidationResults {

    public final long testDurationMS;

    public final LinkedList<ValidatorResult> results;

    public ValidationResults(List<ValidatorResult> results, long testDurationMS) {
        this.results = new LinkedList<>(results);
        this.testDurationMS = testDurationMS;
    }
}
