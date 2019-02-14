package mil.darpa.immortals.analytics.validators;

import mil.darpa.immortals.core.api.ll.phase1.TestResult;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;

/**
 * Created by awellman@bbn.com on 10/19/16.
 */
public interface ValidatorInterface {

    void processEvent(AnalyticsEvent event);

    String getValidatorIdentifier();

    /**
     * Attempts validation
     * @param terminalState If true, currently running validators listening for potential violations should assume they are done listening
     * @return The results
     */
    TestResult attemptValidation(boolean terminalState);

    void start();
}
