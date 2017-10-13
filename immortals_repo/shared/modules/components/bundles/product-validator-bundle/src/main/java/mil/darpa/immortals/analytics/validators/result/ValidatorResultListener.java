package mil.darpa.immortals.analytics.validators.result;

import mil.darpa.immortals.analytics.validators.ValidatorInterface;
import mil.darpa.immortals.core.api.ll.phase1.TestResult;

/**
 * Created by awellman@bbn.com on 10/24/16.
 */
public interface ValidatorResultListener {
    void validationComplete(ValidatorInterface validator, TestResult result);
}
